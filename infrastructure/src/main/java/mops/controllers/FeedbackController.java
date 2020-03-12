package mops.controllers;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Set;
import mops.security.Account;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/feedback/")
public class FeedbackController {
  private final transient Counter publicAccess;

  public FeedbackController(MeterRegistry registry) {
    publicAccess = registry.counter("access.public");
  }

  private Account createAccountFromPrincipal(KeycloakAuthenticationToken token) {
    KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();
    return new Account(
        principal.getName(),
        principal.getKeycloakSecurityContext().getIdToken().getEmail(),
        null,
        token.getAccount().getRoles());
  }

  @GetMapping("/")
  public String index(KeycloakAuthenticationToken token, Model model) {
    if (token != null) {
      model.addAttribute("account", createAccountFromPrincipal(token));
      Set<String> tokenRoles = token.getAccount().getRoles();
      if (tokenRoles.contains("studentin")) {
        return "redirect:/feedback/student/";
      } else if (tokenRoles.contains("orga")) {
        return "redirect:/feedback/dozenten";
      }
    }
    publicAccess.increment();
    return "index";
  }
}
