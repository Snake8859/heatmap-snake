import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.snake.gis.controller"})
public class App {
    public static void  main(String[] args){
        SpringApplication.run(App.class,args);
    }
}
