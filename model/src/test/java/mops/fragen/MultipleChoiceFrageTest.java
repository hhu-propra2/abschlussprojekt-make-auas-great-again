package mops.fragen;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MultipleChoiceFrageTest {

  @Test
  void addChoice() {
    MultipleChoiceFrage frage = new MultipleChoiceFrage("Hallo?");

    frage.addChoice(new Auswahl("Hi"));

    Assertions.assertTrue(frage.containsChoice("Hi"));
  }

  @Test
  void deleteChoice() {
    MultipleChoiceFrage frage = new MultipleChoiceFrage(1L, "Hallo?");
    Auswahl neu = new Auswahl(1L, "Hi");

    frage.addChoice(neu);
    frage.deleteChoice(1L);

    Assertions.assertFalse(frage.getChoices().contains(neu));
  }

  @Test
  void getNumberOfChoices() {
    MultipleChoiceFrage frage = new MultipleChoiceFrage(1L, "Hallo?");

    frage.addChoice(new Auswahl("Hi"));
    frage.addChoice(new Auswahl("Hu"));
    frage.addChoice(new Auswahl("Ha"));

    Assertions.assertEquals(3, frage.getNumberOfChoices());
  }

  @Test
  void addAntwort() {
  }

  @Test
  void getAntworten() {
  }

  @Test
  void testToString() {
  }

  @Test
  void holeErgebnis() {
  }
}