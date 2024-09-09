package org.micro.social.eurekasecurity.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.micro.shareable.response.ResponseHandler;
import org.micro.social.eurekasecurity.dto.JwtRequest;
import org.micro.social.eurekasecurity.dto.JwtResponse;
import org.micro.social.eurekasecurity.dto.RegistrationUserDto;
import org.micro.shareable.dto.UserDto;
import org.micro.social.eurekasecurity.kafka.KafkaUserClient;
import org.micro.social.eurekasecurity.redis.RedisService;
import org.micro.social.eurekasecurity.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;

import java.security.Principal;
import java.util.Optional;
import java.util.Set;

@Service
public class AuthService {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RedisService redisService;

    @Autowired
    private KafkaUserClient kafkaUserClient;

    @Autowired
    private RoleRepository roleRepository;

    @Value("${jwt-expiration-milliseconds}")
    Long lifetime;

    //Метод для входу користувача
    public ResponseEntity<?> login(JwtRequest request, ServerWebExchange exchange) {
        // Отримання UserDto за іменем
        UserDto userDto = kafkaUserClient.getUserByUsername(request.getUsername()).orElse(null);
        String username = userDto != null ? userDto.getUsername() : null;
        if (userDto != null) {

            // Генерація токенів
            String accessToken = jwtUtils.generate(username, userDto.getRoles(), "ACCESS");
            String refreshToken = jwtUtils.generate(username, userDto.getRoles(), "REFRESH");

            // Збереження Refresh токена в Redis
            redisService.saveRefreshToken(username, refreshToken);

            // Додавання токена в cookie
            exchange.getResponse().addCookie(createCookie("AccessToken", accessToken, lifetime));


            // Повернення відповіді з токенами
            JwtResponse jwtResponse = new JwtResponse(accessToken, refreshToken);
            return ResponseHandler.responseBuilder(HttpStatus.OK, jwtResponse, "result");
        }

        return ResponseHandler.responseBuilder(HttpStatus.BAD_REQUEST, "Username or password is incorrect", "result");
    }

    //Метод для реєстрації користувача
    public ResponseEntity<?> createNewUser(RegistrationUserDto newUserCandidate) {
        // Перевірка, чи пароль та підтвердження пароля співпадають
        if (validatePassword(newUserCandidate)) {
            // Конвертація RegistrationUserDto в UserDto
            UserDto userDto = registrationUserToDto(newUserCandidate);
            // Відправка запиту на створення користувача через Kafka
            Optional<String> result = kafkaUserClient.createUser(userDto);

            HttpStatus httpStatus;
            // Перевірка результату створення користувача
            if (result.isPresent() && result.get().equals("Created")) {
                httpStatus = HttpStatus.CREATED;
                return ResponseHandler.responseBuilder(httpStatus, result, "result");
            } else {
                httpStatus = HttpStatus.BAD_REQUEST;
                return ResponseHandler.responseBuilder(httpStatus, result.orElse("Error"), "result");
            }
        } else {
            return ResponseHandler.responseBuilder(HttpStatus.BAD_REQUEST, "Password doesn't match", "result");
        }
    }

    //Оновлює Access токен на основі наданого Refresh токена
    public String refreshToken(String token) {
        return jwtUtils.refreshAccessToken(token);
    }

    //Перевіряє, чи співпадає пароль з підтвердженням пароля
    private Boolean validatePassword(RegistrationUserDto userDto) {
        return userDto.getPassword().equals(userDto.getConfirmPassword());
    }

    //Конвертує RegistrationUserDto в UserDto та хешує пароль користувача за допомогою BCrypt.
    public UserDto registrationUserToDto(RegistrationUserDto registrationUserDto) {
        return new UserDto(
                registrationUserDto.getUsername(),
                BCrypt.hashpw(registrationUserDto.getPassword(), BCrypt.gensalt()),
                registrationUserDto.getEmail(),
                registrationUserDto.getBirthDate(),
                Set.of(roleRepository.findByName("USER").orElseThrow())
        );
    }

    //Метод для створення сookie, яке зберігатиме токен
    public ResponseCookie createCookie(String cookieName, String token, Long durationMillis) {
        return ResponseCookie.from(cookieName, token)
                .httpOnly(true) //Тільки для протоколу HTTP
                .secure(true) //Використовує HTTPS
                .path("/") //Шлях по якому cookie буде доступна (в даному випадку, доступна для всіх шляхів)
                .maxAge(durationMillis / 1000) //Час життя cookie в секундах


                //Lax. Режим, який дозволяє надсилати cookie в межах запитів з того самого домену
                // або з піддомена (наприклад, при навігації користувача).
                // Однак, у випадку запитів, зроблених через сторонні джерела
                // (наприклад, при запитах з форм або посилань з інших сайтів), cookies не будуть відправлені.

                //Strict. cookies не будуть відправлятися при жодних запитах з інших сайтів,
                // навіть якщо користувач перейде на сайт через посилання.
                // Тобто cookie надсилаються лише для запитів, які відбуваються у межах того ж домену
                // (переміщення між сторінками цього ж сайту).

                //None. Cookie будуть відправлятися з будь-якими запитами,
                // включаючи сторонні джерела (запити з інших сайтів, iFrame, запити через форми або JavaScript).
                //Небезпечний через те, що можливе перехоплення cookie з інших сайтів!!!!!
                .sameSite("Lax").build(); //

    }

}
