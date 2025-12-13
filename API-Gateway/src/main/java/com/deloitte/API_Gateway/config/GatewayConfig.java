package com.deloitte.API_Gateway.config;

import com.deloitte.API_Gateway.filter.RbacGatewayFilterFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/**
 * Gateway routing configuration.
 * Defines routes to backend services with RBAC.
 */
@Configuration
public class GatewayConfig {

    @Value("${backend.userservice.url}")
    private String userServiceUrl;

    @Value("${backend.authservice.url}")
    private String authServiceUrl;

    @Value("${backend.appointmentservice.url}")
    private String appointmentServiceUrl;

    @Autowired
    private RbacGatewayFilterFactory rbacFilter;

    /**
     * Configures routes for the API Gateway.
     * Each route can have its own RBAC rules based on roles.
     */
    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                
                // Auth Service Routes (No authentication/RBAC required)
                
                // Signup API - public endpoint
                .route("auth_service_signup", r -> r
                        .path("/api/signup", "/api/signup/**")
                        .filters(f -> f.stripPrefix(0))
                        .uri(authServiceUrl)
                )
                
                // Login API - public endpoint
                .route("auth_service_login", r -> r
                        .path("/api/login", "/api/login/**")
                        .filters(f -> f.stripPrefix(0))
                        .uri(authServiceUrl)
                )
                
                // User Service Routes
                
                // Admin-only routes - user management
                .route("user_service_admin", r -> r
                        .path("/api/users/admin/**")
                        .filters(f -> {
                            RbacGatewayFilterFactory.Config config = new RbacGatewayFilterFactory.Config();
                            config.setAllowedRoles("admin");
                            return f.stripPrefix(0)
                                    .filter(rbacFilter.apply(config));
                        })
                        .uri(userServiceUrl)
                )
                
                // Admin-only route - assign role to users
                .route("user_service_assign_role", r -> r
                        .path("/api/users/*/assignRole")
                        .filters(f -> {
                            RbacGatewayFilterFactory.Config config = new RbacGatewayFilterFactory.Config();
                            config.setAllowedRoles("admin");
                            return f.stripPrefix(0)
                                    .filter(rbacFilter.apply(config));
                        })
                        .uri(userServiceUrl)
                )
                
                // // Doctor-accessible routes - view users
                // .route("user_service_doctor", r -> r
                //         .path("/api/users/doctors/**")
                //         .filters(f -> {
                //             RbacGatewayFilterFactory.Config config = new RbacGatewayFilterFactory.Config();
                //             config.setAllowedRoles("admin,doctor");
                //             return f.stripPrefix(0)
                //                     .filter(rbacFilter.apply(config));
                //         })
                //         .uri(userServiceUrl)
                // )
                
                // // Patient-accessible routes - own profile
                // .route("user_service_patient", r -> r
                //         .path("/api/users/patients/**")
                //         .filters(f -> {
                //             RbacGatewayFilterFactory.Config config = new RbacGatewayFilterFactory.Config();
                //             config.setAllowedRoles("admin,patient");
                //             return f.stripPrefix(0)
                //                     .filter(rbacFilter.apply(config));
                //         })
                //         .uri(userServiceUrl)
                // )
                
                // General user routes - accessible by all authenticated users
                .route("user_service_general", r -> r
                        .path("/api/users/**")
                        .filters(f -> {
                            RbacGatewayFilterFactory.Config config = new RbacGatewayFilterFactory.Config();
                            config.setAllowedRoles("admin,provider,patient,ops");
                            return f.stripPrefix(0)
                                    .filter(rbacFilter.apply(config));
                        })
                        .uri(userServiceUrl)
                )
                
                // Appointment Service Routes
                
                // Provider schedule management - accessible by admin and providers
                .route("appointment_service_schedules_create", r -> r
                        .path("/api/schedules/create-schedule")
                        .and()
                        .method("POST")
                        .filters(f -> {
                            RbacGatewayFilterFactory.Config config = new RbacGatewayFilterFactory.Config();
                            config.setAllowedRoles("ops");
                            return f.stripPrefix(0)
                                    .filter(rbacFilter.apply(config));
                        })
                        .uri(appointmentServiceUrl)
                )
                
                // Get available slots - accessible by all authenticated users
                .route("appointment_service_available_slots", r -> r
                        .path("/api/schedules/provider/*/available-slots")
                        .filters(f -> {
                            RbacGatewayFilterFactory.Config config = new RbacGatewayFilterFactory.Config();
                            config.setAllowedRoles("patient,ops");
                            return f.stripPrefix(0)
                                    .filter(rbacFilter.apply(config));
                        })
                        .uri(appointmentServiceUrl)
                )
                
                // Get provider schedules - accessible by admin and providers
                .route("appointment_service_provider_schedules", r -> r
                        .path("/api/schedules/provider/**")
                        .filters(f -> {
                            RbacGatewayFilterFactory.Config config = new RbacGatewayFilterFactory.Config();
                            config.setAllowedRoles("provider");
                            return f.stripPrefix(0)
                                    .filter(rbacFilter.apply(config));
                        })
                        .uri(appointmentServiceUrl)
                )
                
                // Create appointment - accessible by patients
                .route("appointment_service_create", r -> r
                        .path("/api/appointments/create-appointment")
                        .and()
                        .method("POST")
                        .filters(f -> {
                            RbacGatewayFilterFactory.Config config = new RbacGatewayFilterFactory.Config();
                            config.setAllowedRoles("patient");
                            return f.stripPrefix(0)
                                    .filter(rbacFilter.apply(config));
                        })
                        .uri(appointmentServiceUrl)
                )
                
                // Provider updates appointment status (confirm/reject)
                .route("appointment_service_provider_update", r -> r
                        .path("/api/appointments/*/provider/*/update-status")
                        .filters(f -> {
                            RbacGatewayFilterFactory.Config config = new RbacGatewayFilterFactory.Config();
                            config.setAllowedRoles("provider");
                            return f.stripPrefix(0)
                                    .filter(rbacFilter.apply(config));
                        })
                        .uri(appointmentServiceUrl)
                )
                
                // Patient cancels appointment
                .route("appointment_service_patient_cancel", r -> r
                        .path("/api/appointments/*/patient/*/cancel")
                        .filters(f -> {
                            RbacGatewayFilterFactory.Config config = new RbacGatewayFilterFactory.Config();
                            config.setAllowedRoles("patient");
                            return f.stripPrefix(0)
                                    .filter(rbacFilter.apply(config));
                        })
                        .uri(appointmentServiceUrl)
                )
                
                // Get patient appointments
                .route("appointment_service_patient_appointments", r -> r
                        .path("/api/appointments/patient/**")
                        .filters(f -> {
                            RbacGatewayFilterFactory.Config config = new RbacGatewayFilterFactory.Config();
                            config.setAllowedRoles("patient");
                            return f.stripPrefix(0)
                                    .filter(rbacFilter.apply(config));
                        })
                        .uri(appointmentServiceUrl)
                )
                
                // Get provider appointments
                .route("appointment_service_provider_appointments", r -> r
                        .path("/api/appointments/provider/**")
                        .filters(f -> {
                            RbacGatewayFilterFactory.Config config = new RbacGatewayFilterFactory.Config();
                            config.setAllowedRoles("provider");
                            return f.stripPrefix(0)
                                    .filter(rbacFilter.apply(config));
                        })
                        .uri(appointmentServiceUrl)
                )
                
                // Get specific appointment by ID
                .route("appointment_service_get_by_id", r -> r
                        .path("/api/appointments/*")
                        .and()
                        .method("GET")
                        .filters(f -> {
                            RbacGatewayFilterFactory.Config config = new RbacGatewayFilterFactory.Config();
                            config.setAllowedRoles("provider,patient");
                            return f.stripPrefix(0)
                                    .filter(rbacFilter.apply(config));
                        })
                        .uri(appointmentServiceUrl)
                )
                .build();
    }
}


