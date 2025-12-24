package org.example.controller;

import org.example.model.Card;
import org.example.model.User;
import org.example.repository.CardRepository;
import org.example.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Random;

@Controller
public class CardController {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final Random random = new Random();

    public CardController(CardRepository cardRepository, UserRepository userRepository) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.random.setSeed(System.currentTimeMillis());
    }

    @GetMapping("/")
    public String index(Model model, 
                        @RequestParam(defaultValue = "0") int page,
                        @AuthenticationPrincipal UserDetails userDetails) {
        
        if (cardRepository.count() == 0) {
            cardRepository.save(new Card("Hello", "Привет"));
            cardRepository.save(new Card("World", "Мир"));
            cardRepository.save(new Card("Java", "Джава"));
            cardRepository.save(new Card("Spring", "Спринг"));
        }

        int pageSize = 1;
        Page<Card> cardPage = cardRepository.findNotLearnedByUsername(userDetails.getUsername(), PageRequest.of(page, pageSize));
        
        if (cardPage.hasContent()) {
            model.addAttribute("card", cardPage.getContent().get(0));
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", cardPage.getTotalPages());
            model.addAttribute("showTranslationFirst", random.nextBoolean());
        } else {
            model.addAttribute("allLearned", true);
        }

        return "index";
    }

    @GetMapping("/add")
    public String addCardForm(Model model) {
        model.addAttribute("card", new Card());
        return "add-card";
    }

    @PostMapping("/add")
    public String addCard(@ModelAttribute Card card) {
        cardRepository.save(card);
        return "redirect:/";
    }

    @PostMapping("/learn/{id}")
    public String markAsLearned(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        Card card = cardRepository.findById(id).orElseThrow();
        
        user.getLearnedCards().add(card);
        userRepository.save(user);
        
        return "redirect:/";
    }

    @GetMapping("/learned")
    public String learnedCards(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        model.addAttribute("cards", user.getLearnedCards());
        return "learned";
    }

    @PostMapping("/learned/delete/{id}")
    public String removeLearnedCard(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        Card card = cardRepository.findById(id).orElseThrow();
        
        user.getLearnedCards().remove(card);
        userRepository.save(user);
        
        return "redirect:/learned";
    }
    
    @PostMapping("/delete/{id}")
    public String deleteCard(@PathVariable Long id) {
        cardRepository.deleteById(id);
        return "redirect:/";
    }
}