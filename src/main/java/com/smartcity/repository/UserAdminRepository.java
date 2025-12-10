package com.smartcity.repository;

import com.smartcity.model.UserAdmin;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserAdminRepository extends MongoRepository<UserAdmin, String> {
    
    @Query("{ 'email' : ?0 }")
    Optional<UserAdmin> findByEmail(String email);
    
    // Helper method that returns UserAdmin directly (not Optional)
    default UserAdmin findUserAdminByEmail(String email) {
        return findByEmail(email).orElse(null);
    }
    
    @Query("{ 'department' : ?0 }")
    List<UserAdmin> findByDepartment(String department);
    
    @Query(value = "{ 'email' : ?0 }", exists = true)
    boolean existsByEmail(String email);
}