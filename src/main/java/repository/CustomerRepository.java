package repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import entities.Customer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    
    // Buscar por número de cliente (debe ser único)
    Optional<Customer> findByCustomerNumber(String customerNumber);
    
    // Buscar por DNI/NIF
    Optional<Customer> findByDocumentNumber(String documentNumber);
    
    // Buscar por email
    Optional<Customer> findByEmail(String email);
    
    // Buscar clientes activos
    List<Customer> findByActiveTrue();
    
    // Buscar por nombre (búsqueda flexible)
    List<Customer> findByFirstNameContainingIgnoreCase(String firstName);
    List<Customer> findByLastNameContainingIgnoreCase(String lastName);
    
    // Búsqueda combinada nombre/apellido
    @Query("SELECT c FROM Customer c WHERE " +
           "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Customer> searchByName(@Param("query") String query);
    
    // Verificar existencia
    boolean existsByCustomerNumber(String customerNumber);
    boolean existsByEmail(String email);
    boolean existsByDocumentNumber(String documentNumber);
    
    // Contar clientes activos
    long countByActiveTrue();
}