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

    private static final String key = "0913idg91";

    public static void parser(String token) throws SignatureException {
        Jwts.parser().setSigningKey(key.getBytes()).parseClaimsJws(token);
    }

    public static String getSubject(String token) {
        return Jwts.parser().setSigningKey(key.getBytes()).parseClaimsJws(token).getBody().getSubject();
    }

    public static String token(String s) {
        return Jwts.builder()
                .setSubject(s)
                .signWith(SignatureAlgorithm.HS512, key.getBytes())
                .compact();
    }

    public static void main(String[] args) {
        System.out.println(token("{\"userId\":1234}"));
        System.out.println(token("{\"userId\":5678}"));
    }
}
