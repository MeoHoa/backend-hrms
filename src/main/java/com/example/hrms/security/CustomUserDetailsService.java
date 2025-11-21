package com.example.hrms.security;

import com.example.hrms.entity.User;
import com.example.hrms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        // login can be either username or email
        User u = userRepository.findByUsernameOrEmail(login)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + login));
        List<GrantedAuthority> auth = List.of(new SimpleGrantedAuthority(u.getRole()==null?"":u.getRole().getRoleKey()));
        // Use email as the principal for Spring Security
        return org.springframework.security.core.userdetails.User.withUsername(u.getEmail()).password(u.getPassword()).authorities(auth).build();
    }
}
