package im.web.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;

import im.authentication.TokenGenerate;
import im.web.servlet.vo.Result;

/**
 * LoginServlet
 * Date: 2018-06-12
 *
 * @author zouchuanhua
 */
@WebServlet(urlPatterns = "/login")
public class LoginServlet extends HttpServlet {

    private static final Map<String,String> map = Maps.newHashMap();

    static {
        map.put("11","11");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {


        String username = req.getParameter("username");
        String password = req.getParameter("password");

        Result<LoginResult> result = new Result<>();
        if(StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            result.setMsg("username or password is empty");
            resp.getWriter().write(JSON.toJSONString(result));
            return;
        }

        if(map.get(username) == null) {
            result.setMsg("username or password incorrect");
            resp.getWriter().write(JSON.toJSONString(result));
            return;
        }
        LoginResult loginResult = new LoginResult();
        String token = TokenGenerate.token("{\"username\":\""+ username +"\"}");
        loginResult.setToken(token);
        result.setSuccess(true);
        result.setData(loginResult);
        resp.getWriter().write(JSON.toJSONString(result));

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    private static final class LoginResult {
        private String token;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}
