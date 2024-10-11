@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @RestController
    class HelloController {
        @GetMapping("/hello")
        public String hello() {
            return "Hello, World!";
        }
    }
}