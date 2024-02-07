package br.com.projeto;




import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm;
*/

@SpringBootApplication
public class Startup {

    public static void main(String[] args) {
        SpringApplication.run(Startup.class, args);
        
        /*
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        
        Pbkdf2PasswordEncoder pbkdf2Encoder =
        		new Pbkdf2PasswordEncoder(
    				"", 8, 185000,
    				SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256);
        
        encoders.put("pbkdf2", pbkdf2Encoder);
        DelegatingPasswordEncoder passwordEncoder = new DelegatingPasswordEncoder("pbkdf2", encoders);
        passwordEncoder.setDefaultPasswordEncoderForMatches(pbkdf2Encoder);
        
        String result1 = passwordEncoder.encode("admin123");
        String result2 = passwordEncoder.encode("admin234");
        System.out.println("My hash result1 " + result1);
        System.out.println("My hash result2 " + result2);
        
        
//        My hash result1 {pbkdf2}b02ccb4d284685adcd7d246843ddaa4d9da2af752ad8143f34445413a945881d4e04dd3c323ffa52
//		My hash result2 {pbkdf2}d58620cee01488b41a36e1796a95a7826e301cd3fa3d9528dfc3153c1b9110ea476421bfddb51f5f
        
//        My hash result1 {pbkdf2}56794b77a052da52a4fc074e601bc1e0790eb9479314e6b99fefc180ebcd0b43dc1e48868808593a
//        My hash result2 {pbkdf2}8df8ad216e729442909032d7795f73198015de014c4802f83ee322ef8234fef4c2f5cc0e450d1d2a

        // My hash result1 {pbkdf2}9477dd0c142ca12e04647736b8f82d99e2339a0da260542d2f8ca31c960561a0a03f27fb598be3cc
        // My hash result2 {pbkdf2}6665ff51d8b97abab00d17540a58ef680fc43cb50cf6fafeba6b11aee62b0085a950b59237d1497b
        
//        My hash result1 {pbkdf2}e7595404f76610bb5dee2279959b2686741def4a9200378da298df09dfcfb9896233a1fb8ff76b09
//        My hash result2 {pbkdf2}6f2ab178df1c2eac9b897eda5eac68ca35ec82b2f69c30386176e76f44a96729d0ab51ed348a1934
        */
        
    }
}
