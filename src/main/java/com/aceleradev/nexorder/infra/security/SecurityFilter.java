package com.aceleradev.nexorder.infra.security;

import com.aceleradev.nexorder.commons.enums.Role;
import com.aceleradev.nexorder.entities.Employee;
import com.aceleradev.nexorder.repositories.EmployeeRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;

    private final EmployeeRepository repository;

    // @Override
    //protected boolean shouldNotFilter(HttpServletRequest request) {
      //  return request.getServletPath().equals("/auth/login");
    //}

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = recoverToken(request);

        if (token != null) {
            String email = tokenService.validateToken(token);

            if (email != null) {
                Employee user = repository.findByEmail(email)
                        .orElse(null);

                if (user != null) {
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

                    if (user.getRole() == Role.MANAGER || user.getRole() == Role.SELLER || user.getRole() == Role.INTERN) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_EMPLOYEE"));
                    }

                    // ADICIONE ESTA LINHA PARA DEBUG:
                    System.out.println("Usuário: " + email + " | Authorities: " + authorities);

                    var authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        return authHeader.replace("Bearer ", "");
    }
}