package com.creatoros.security;

import com.creatoros.repository.CreatorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreatorUserDetailsService implements UserDetailsService {

    private final CreatorRepository creatorRepository;

    @Override
    @Transactional(readOnly = true)
    public CreatorPrincipal loadUserByUsername(String email) throws UsernameNotFoundException {
        return creatorRepository.findByEmailIgnoreCase(email).map(CreatorPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("No creator with email " + email));
    }
}
