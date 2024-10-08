package com.example.Word_Lab;





import jakarta.servlet.http.HttpSession;
import jakarta.websocket.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class UserController {
    @Autowired
    private WordRepository wordRepository;
    @GetMapping("/dashboard")
    public String showForm(HttpSession session, Model model) {

        User user = (User) session.getAttribute("user");
        String winmessage = (String) session.getAttribute("win_message");
        if (user != null) {
            model.addAttribute("user", user);
            model.addAttribute("levels", new String[]{"Easy", "Medium", "Hard"});
            model.addAttribute("selectedLevel", "");
            model.addAttribute("user", user);
            model.addAttribute("message",winmessage);
            return "word-form";
        } else {
            return "redirect:/";
        }

    }
    @PostMapping("/word")
    public String getWord(@ModelAttribute("selectedLevel") String selectedLevel,
                          Model model, HttpSession session) {

        User user = (User) session.getAttribute("user");
        session.setAttribute("win_message", null);
        if (user != null) {
            Word word = wordRepository.findRandomWordByLevel(selectedLevel);
            model.addAttribute("word", word);
            session.setAttribute("word", word);
            model.addAttribute("user", user);
            // return "wordDetails";
            return "redirect:/showWord";
        } else {
            return "redirect:/";
        }

    }
    @GetMapping("/showWord")
    public String showWord(HttpSession session, Model model) {


        User user = (User) session.getAttribute("user");
        if (user != null) {
            Word wordarray = (Word) session.getAttribute("word");
            model.addAttribute("GivenHints", wordarray.getHints());
            model.addAttribute("GivenImage", wordarray.getImage());
            model.addAttribute("user", user);
            return "word-input";
        } else {
            return "redirect:/";
        }
    }
    @PostMapping("/getWord")
    public String checkWordGuess(@RequestParam String word, HttpSession session, Model model) {

        User user = (User) session.getAttribute("user");
        if (user != null) {
            Word wordarray = (Word) session.getAttribute("word");
            Integer attempts = (Integer) session.getAttribute("attempts");

            if (attempts == null) {
                attempts = 0; // Initialize if not set
            }

            model.addAttribute("GivenHints", wordarray.getHints());
            model.addAttribute("GivenImage", wordarray.getImage());

            // Check if the user's guess matches the actual word
            if (word != null && wordarray.getWordName().equalsIgnoreCase(word)) {
                model.addAttribute("message", "Congratulations! You win.");
                model.addAttribute("user", user);
                Integer score = Math.toIntExact(user.getScore() + 10);

                // Reset attempts on a correct guess
                session.setAttribute("attempts", 0);

                return "redirect:/Score/" + user.getId() + "/" + score;
            } else {
                // Increment the attempt count
                attempts += 1;
                session.setAttribute("attempts", attempts);

                if (attempts < 3) {
                    // User has more chances
                    model.addAttribute("message", "Wrong word, try again.");
                    model.addAttribute("user", user);
                    return "word-input";
                } else {
                    // User has exhausted all attempts - select a new word and reset attempts
                    session.setAttribute("attempts", 0); // Reset attempts
                    model.addAttribute("message", "Sorry! You've used all 3 attempts. A new word has been assigned.");

                    // Load a new word
                    Word newWord = wordRepository.findRandomWordByLevel("Easy"); // Adjust level as needed
                    session.setAttribute("word", newWord);
                    model.addAttribute("GivenHints", newWord.getHints());
                    model.addAttribute("GivenImage", newWord.getImage());
                    model.addAttribute("user", user);

                    return "word-input";
                }
            }
        } else {
            return "redirect:/";
        }
    }


}





