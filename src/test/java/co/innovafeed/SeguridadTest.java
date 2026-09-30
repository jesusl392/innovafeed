package co.innovafeed;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integración de la seguridad: la app arranca completa (con los datos del
 * DataLoader) y se simulan peticiones HTTP con MockMvc.
 * Usa su propia base H2 en memoria para no mezclarse con otras pruebas.
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:seguridad;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@DisplayName("Seguridad: login y acceso por rol")
class SeguridadTest {

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("El catálogo y los planes son públicos")
    void paginasPublicas() throws Exception {
        mvc.perform(get("/catalogo")).andExpect(status().isOk());
        mvc.perform(get("/planes")).andExpect(status().isOk());
        mvc.perform(get("/privacidad")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Sin sesión, las zonas privadas redirigen al login")
    void zonasPrivadasPidenLogin() throws Exception {
        mvc.perform(get("/admin")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
        mvc.perform(get("/usuario/favoritos")).andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "USUARIO")
    @DisplayName("Un USUARIO no puede entrar al panel de admin ni al del emprendedor (403)")
    void usuarioNoEntraAZonasAjenas() throws Exception {
        mvc.perform(get("/admin")).andExpect(status().isForbidden());
        mvc.perform(get("/emprendedor")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("El ADMIN sí entra a su panel")
    void adminEntraAlPanel() throws Exception {
        mvc.perform(get("/admin")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Login correcto: cada rol va a su panel")
    void loginRedirigePorRol() throws Exception {
        mvc.perform(formLogin("/login").user("email", "admin@innovafeed.test").password("Admin123*"))
                .andExpect(redirectedUrl("/admin"));
        mvc.perform(formLogin("/login").user("email", "laura@innovafeed.test").password("Emprende123*"))
                .andExpect(redirectedUrl("/emprendedor"));
        mvc.perform(formLogin("/login").user("email", "ana@innovafeed.test").password("Usuario123*"))
                .andExpect(redirectedUrl("/usuario"));
    }

    @Test
    @DisplayName("Login con contraseña incorrecta es rechazado")
    void loginIncorrecto() throws Exception {
        mvc.perform(formLogin("/login").user("email", "ana@innovafeed.test").password("equivocada"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"));
    }
}
