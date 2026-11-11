package com.example.QLPT.Repository;


import com.example.QLPT.Enity.Enum.Enum;
import com.example.QLPT.Enity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByReferenceIdAndReferenceType(Long referenceId, Enum.ReferenceType type);
}
