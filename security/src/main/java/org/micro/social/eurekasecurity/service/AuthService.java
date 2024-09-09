package org.micro.social.eurekasecurity.service;

import org.micro.social.eurekasecurity.dto.JwtRequest;
import org.micro.social.eurekasecurity.dto.RegistrationUserDto;
import org.micro.shareable.dto.UserDto;
import org.micro.social.eurekasecurity.kafka.KafkaUserClient;
import org.micro.social.eurekasecurity.redis.RedisService;
import org.micro.social.eurekasecurity.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

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
    public String login(JwtRequest request, ServerWebExchange exchange) {
        // Отримання UserDto за іменем
        UserDto userDto = kafkaUserClient.getUserByUsername(request.getUsername()).orElse(null);

        if (userDto != null) {
            String username = userDto.getUsername();
            // Генерація токенів
            String accessToken = jwtUtils.generate(username, userDto.getRoles(), "ACCESS");
            String refreshToken = jwtUtils.generate(username, userDto.getRoles(), "REFRESH");

            // Збереження Refresh токена в Redis
            redisService.saveRefreshToken(username, refreshToken);

            // Додавання токена в cookie
            exchange.getResponse().addCookie(createCookie("AccessToken", accessToken, lifetime));
            // Повернення відповіді з токенами
            return "Вхід успішний";
        }

        return "Пароль або логін невірний";
    }

    //Метод для реєстрації користувача
    public String createNewUser(RegistrationUserDto newUserCandidate) {
        // Перевірка, чи пароль та підтвердження пароля співпадають
        if (validatePassword(newUserCandidate)) {
            // Конвертація RegistrationUserDto в UserDto
            UserDto userDto = registrationUserToDto(newUserCandidate);
            // Відправка запиту на створення користувача через Kafka
            Optional<String> result = kafkaUserClient.createUser(userDto);

            // Перевірка результату створення користувача
            if (result.isPresent() && result.get().equals("Created")) {
                return "Створено";
            } else {
                return "Помилка";
            }
        } else {
            return "Пароль не співпадає";
        }
    }

    public String logout(ServerWebExchange exchange) {

        String token = exchange.getResponse().getCookies().get("AccessToken").get(0).getValue();

        ResponseCookie accessTokenCookie = ResponseCookie.from("AccessToken", "")
                .maxAge(0)
                .path("/")
                .build();
        exchange.getResponse().addCookie(accessTokenCookie);
        //Видаляється RefreshToken користувача (Користувач отримується з claims токена) з Redis бази даних
        redisService.deleteToken(jwtUtils.getAllClaimsFromToken(token).get("username", String.class));
        return "OK";
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
