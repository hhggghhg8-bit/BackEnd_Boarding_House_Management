package com.example.QLPT.Repository;

import com.example.QLPT.Enity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email); // Dùng để đăng nhập
    List<User> findByRole(String role);       // Lấy danh sách admin/chủ trọ/người thuê
}
