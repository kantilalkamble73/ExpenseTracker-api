package com.expensemanager.service;

import com.expensemanager.dto.AuthResponse;
import com.expensemanager.dto.LoginRequest;
import com.expensemanager.dto.RegisterRequest;
import com.expensemanager.entity.Category;
import com.expensemanager.entity.User;
import com.expensemanager.exception.BadRequestException;
import com.expensemanager.repository.CategoryRepository;
import com.expensemanager.repository.UserRepository;
import com.expensemanager.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    private static final List<String[]> DEFAULT_CATEGORIES = List.of(
            new String[]{"Food & Dining", "utensils", "#F59E0B"},
            new String[]{"Transportation", "car", "#3B82F6"},
            new String[]{"Shopping", "shopping-bag", "#EC4899"},
            new String[]{"Entertainment", "film", "#8B5CF6"},
            new String[]{"Bills & Utilities", "file-text", "#EF4444"},
            new String[]{"Healthcare", "heart", "#10B981"},
            new String[]{"Education", "book", "#6366F1"},
            new String[]{"Rent", "home", "#0EA5E9"},
            new String[]{"Travel", "map-pin", "#14B8A6"},
            new String[]{"Others", "tag", "#64748B"}
    );

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setMonthlyIncome(request.getMonthlyIncome());
        user = userRepository.save(user);

        // Seed default categories for the new user (smart onboarding)
        for (String[] c : DEFAULT_CATEGORIES) {
            Category category = new Category();
            category.setName(c[0]);
            category.setIcon(c[1]);
            category.setColor(c[2]);
            category.setUser(user);
            category.setIsDefault(false);
            categoryRepository.save(category);
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getId());
        return new AuthResponse(token, user.getId(), user.getFullName(), user.getEmail(), user.getMonthlyIncome());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        String token = jwtUtil.generateToken(user.getEmail(), user.getId());
        return new AuthResponse(token, user.getId(), user.getFullName(), user.getEmail(), user.getMonthlyIncome());
    }
}
