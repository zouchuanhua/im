package im.authentication;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;

/**
 * TokenGenerate
 * Date: 2018-06-11
 *
 * @author zouchuanhua
 */
public class TokenGenerate {


    public static void main(String[] args) {
        String token = Jwts.builder()
                .setSubject("123")
                .signWith(SignatureAlgorithm.HS512, "111".getBytes())
                .compact();
        System.out.println(token);


        try {
            String subject = Jwts.parser().
                    setSigningKey("111".getBytes()).
                    parseClaimsJws("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI1Njc4In0=.dp5Nz1sovMxDOLpGnXyjYccMU7n0iLdTjpc4dBCwF3IKmQL0SCzdwPJY0bhw4yTJODeVhssbz_vd5JRXnCv91g")
            .getBody().getSubject();
            System.out.println(subject);
        }catch (SignatureException e) {
            e.printStackTrace();
        }

    }

    public static void parser(String token) throws SignatureException{
        Jwts.parser().
                    setSigningKey("111".getBytes()).
                    parseClaimsJws(token);
    }

    public static String token(String s) {
        return Jwts.builder()
                .setSubject(s)
                .signWith(SignatureAlgorithm.HS512, "111".getBytes())
                .compact();
    }
}
