package com.newwork.employeeprofile.security;

import com.newwork.employeeprofile.domain.Employee;
import com.newwork.employeeprofile.domain.Permission;
import com.newwork.employeeprofile.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private Long id;
    private String email;
    private String password;
    private Role role;
    private Collection<? extends GrantedAuthority> authorities;

    public static UserPrincipal create(Employee employee) {
        Role role = employee.getRole();

        List<GrantedAuthority> authorities = new ArrayList<>();

        // Add role as authority
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));

        // Add each permission as authority
        for (Permission permission : role.getPermissions()) {
            authorities.add(new SimpleGrantedAuthority(permission.getName()));
        }

        return new UserPrincipal(
                employee.getId(),
                employee.getEmail(),
                employee.getPassword(),
                role,
                authorities
        );
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
