package mops.controllers;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import mops.Frage;
import mops.Fragebogen;
import mops.Kontaktformular;
import mops.Student;
import mops.SubmitService;
import mops.TypeChecker;
import mops.database.MockDozentenRepository;
import mops.security.Account;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/feedback/student")
@Controller
@SuppressWarnings("checkstyle:MissingJavadocMethod")
public class StudentController {
  private static final String emptySearchString = "";
  private final transient String account = "account";
  private final transient Counter authenticatedAccess;
  private final transient SubmitService submitService;
  @Autowired
  @Qualifier("Faker")
  private transient FragebogenRepository frageboegen;
  private transient DozentenRepository dozenten;
  private transient TypeChecker typeChecker = new TypeChecker();

  public StudentController(MeterRegistry registry) {
    authenticatedAccess = registry.counter("access.authenticated");
    dozenten = new MockDozentenRepository();
    submitService = new SubmitService();
  }

  @GetMapping("/")
  @RolesAllowed("ROLE_studentin")
  public String uebersicht(KeycloakAuthenticationToken token, Model model, String search) {
    if (searchNotEmpty(search)) {
      model.addAttribute("frageboegen", frageboegen.getAllContaining(search));
    } else {
      model.addAttribute("frageboegen", frageboegen.getAll());
    }
    model.addAttribute("typeChecker", typeChecker);
    model.addAttribute(account, createAccountFromPrincipal(token));
    authenticatedAccess.increment();
    return "studenten/student_uebersicht";
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  @GetMapping("/details")
  @RolesAllowed("ROLE_studentin")
  public String fragebogen(KeycloakAuthenticationToken token, Model model, @RequestParam Long
      id) {
    model.addAttribute("fragebogen", frageboegen.getFragebogenById(id));
    model.addAttribute("typeChecker", typeChecker);
    model.addAttribute(account, createAccountFromPrincipal(token));
    authenticatedAccess.increment();
    return "studenten/student_details";
  }

  @PostMapping("/details/submit/{bogennr}")
  @RolesAllowed("ROLE_studentin")
  public String submitFeedback(KeycloakAuthenticationToken token, @PathVariable long bogennr, HttpServletRequest req, Model model) {
    Fragebogen fragebogen = frageboegen.getFragebogenById(bogennr);
    Map<Long, String> antworten = new HashMap<>();
    for (Frage frage : fragebogen.getFragen()) {
      antworten.put(frage.getId(), req.getParameter("answer-" + frage.getId()));
    }
    submitService.saveAntworten(fragebogen, antworten);
    model.addAttribute(account, createAccountFromPrincipal(token));
    authenticatedAccess.increment();
    return "redirect:/feedback/student/";
  }

  @GetMapping("/kontakt")
  @RolesAllowed("ROLE_studentin")
  public String kontakt(KeycloakAuthenticationToken token, Model model) {
    model.addAttribute("dozenten", dozenten.getAll());
    model.addAttribute("kontaktformular", new Kontaktformular());
    model.addAttribute(account, createAccountFromPrincipal(token));
    authenticatedAccess.increment();
    return "/studenten/kontakt";
  }

  @PostMapping("/kontakt")
  @RolesAllowed("ROLE_studentin")
  public String postMessage(KeycloakAuthenticationToken token, @ModelAttribute Kontaktformular kontakt, Model model) {
    kontakt.setStudent(new Student(678L, "a", "b", "a@b.de", 2278));
    kontakt.setZeitpunkt(LocalDateTime.now());
    model.addAttribute(account, createAccountFromPrincipal(token));
    authenticatedAccess.increment();
    return "redirect:/feedback/student/";
  }

  @GetMapping("/ergebnis")
  @RolesAllowed("ROLE_studentin")
  public String boegenUebersicht(KeycloakAuthenticationToken token, Model model) {
    model.addAttribute("frageboegen", frageboegen.getAll());
    model.addAttribute("typeChecker", typeChecker);
    model.addAttribute(account, createAccountFromPrincipal(token));
    authenticatedAccess.increment();
    return "/studenten/ergebnis";
  }


  @GetMapping("/ergebnisUebersicht")
  @RolesAllowed("ROLE_studentin")
  public String ergebnisUebersicht(KeycloakAuthenticationToken token, @RequestParam long id, Model model) {
    Fragebogen fragebogen = frageboegen.getFragebogenById(id);
    model.addAttribute("fragebogen", fragebogen);
    model.addAttribute("textFragen", fragebogen.getTextfragen());
    model.addAttribute("multipleChoiceFragen", fragebogen.getMultipleChoiceFragen());
    model.addAttribute(account, createAccountFromPrincipal(token));
    authenticatedAccess.increment();
    return "/studenten/ergebnisUebersicht";
  }

  private boolean searchNotEmpty(String search) {
    return !emptySearchString.equals(search) && search != null;
  }


  private Account createAccountFromPrincipal(KeycloakAuthenticationToken token) {
    KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();
    return new Account(
        principal.getName(),
        principal.getKeycloakSecurityContext().getIdToken().getEmail(),
        null,
        token.getAccount().getRoles());
  }
}
