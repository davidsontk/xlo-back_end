package br.com.xlo.security;

import br.com.xlo.model.Usuario;
import static br.com.xlo.security.SecurityConstants.EXPIRATION_TIME;
import static br.com.xlo.security.SecurityConstants.SECRET;
import static br.com.xlo.security.SecurityConstants.TOKEN_PREFIX;
import br.com.xlo.usuario.Login;
import br.com.xlo.usuario.UserDetailsServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jboss.logging.Logger;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 *
 * @author Davidson
 */
public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private static final Logger log = Logger.getLogger(JWTAuthenticationFilter.class);
    private final int SALTS = 10;
    private AuthenticationManager authenticationManager;
    private UserDetailsServiceImpl userDetailsService;

    public JWTAuthenticationFilter(AuthenticationManager authenticationManager, UserDetailsServiceImpl userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res) throws AuthenticationException {
        try {
            Usuario creds = new ObjectMapper()
                    .readValue(req.getInputStream(), Usuario.class);

            User u = (User) userDetailsService.loadByEmail(creds.getEmail());

            if (!checkPassword(creds.getPassword(), u.getPassword())) {
                res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access Denied");
            } else {
                creds.setUsername(u.getUsername());
                creds.setEnable(true);
                for (GrantedAuthority object : u.getAuthorities()) {
                    creds.setPerfil(object.getAuthority());
                }

                Authentication token = new UsernamePasswordAuthenticationToken(creds,
                        creds.getPassword());

                return token;
            }

            return null;

        } catch (IOException e) {
            log.error("Erro ao autenticar usuario", e);
            throw new RuntimeException(e);
        }

    }

    @Override
    protected void successfulAuthentication(HttpServletRequest req,
            HttpServletResponse res,
            FilterChain chain,
            Authentication auth) throws IOException, ServletException {

        Usuario u = (Usuario) auth.getPrincipal();

        String token = Jwts.builder()
                .setSubject(u.getUsername())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, SECRET.getBytes())
                .compact();

        ObjectMapper mapper = new ObjectMapper();
        AccountCredentials account = new AccountCredentials();
        account.setSuccess(true);

        Login login = new Login();

        login.setEmail(u.getEmail());
        login.setName(u.getUsername());
        login.setPerfil(u.getPerfil());
        login.setToken(TOKEN_PREFIX + token);
        account.setUsuario(login);

        String user = mapper.writeValueAsString(account);
        res.getWriter().append(user);

        // res.addHeader(HEADER_STRING, TOKEN_PREFIX + token);
    }

    private boolean checkPassword(String senha, String senhaPersistida) {
        try {
            //return BCrypt.checkpw(senha, senhaPersistida);//(senha, senhaPersistida);
            return true;
        } catch (Exception e) {
            log.error("Erro ao comparar senha", e);
            return false;
        }

    }

}