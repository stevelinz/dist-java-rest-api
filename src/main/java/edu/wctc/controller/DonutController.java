package edu.wctc.controller;


import edu.wctc.entity.Donut;
import edu.wctc.service.DonutService;
import edu.wctc.service.DonutShopService;
import edu.wctc.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/donut")
public class DonutController {
    // Inject the Donut Service
    @Autowired
    private DonutService donutService;

    @Autowired
    private ImageService imageService;

    // Inject the Donut Shop service
    @Autowired
    private DonutShopService donutShopService;

    @GetMapping("/delete")
    public String deleteDonut(@RequestParam("donutId") int theId) {
        // Delete the donut
        donutService.deleteDonut(theId);

        return "redirect:/donut/list";
    }

    @GetMapping("/list")
    public String listDonuts(Model theModel) {
        // Get donuts from service
        List<Donut> donutList = donutService.getDonuts();

        // Add the list of donuts to the model
        theModel.addAttribute("donuts", donutList);

        // Return the name of the view
        return "list-donuts";
    }

    @RequestMapping("/user/showAddDonutForm")
    public String showAddDonutForm(Model theModel) {
        Donut theDonut = new Donut();

        theModel.addAttribute("aDonut", theDonut);

        theModel.addAttribute("donutShops", donutShopService.getDonutShops());

        return "donut-form";
    }

    @RequestMapping("/showUpdateDonutForm")
    public String showUpdateDonutForm(@RequestParam("donutId") int theId,
                                      Model theModel) {
        // Get donut from the database
        Donut theDonut = donutService.getDonut(theId);

        // Set donut as a model attribute to pre-populate the form
        theModel.addAttribute("aDonut", theDonut);

        theModel.addAttribute("donutShops", donutShopService.getDonutShops());

        // Return the view
        return "donut-form";
    }


    @PostMapping("/save")
    public String saveDonut(@RequestParam("imageFile") MultipartFile file,
                            @Valid @ModelAttribute("donut") Donut theDonut,
                            BindingResult bindingResult,
                            Model theModel) {
        // Any validation errors?
        if (bindingResult.hasErrors()) {
            // Display the errors in the console
            System.err.println(bindingResult);

            // Put list of donut shops back in the new model
            theModel.addAttribute("donutShops", donutShopService.getDonutShops());

            // Send back to form with error messages
            return "donut-form";
        }

        // Use the other service to save the image
        int imageId = imageService.saveImage(file);

        // Update the donut
        donutService.saveDonut(theDonut, imageId);

        // Clean up any images no longer referenced by donuts
        imageService.deleteUnusedImages();

        // Redirect back to the donut list
        return "redirect:/donut/list";
    }

    @GetMapping("/search")
    public String search(@RequestParam("searchTerm") String theSearchTerm, Model theModel) {
        List<Donut> matchingDonuts = donutService.getDonutsByName(theSearchTerm);

        theModel.addAttribute("donuts", matchingDonuts);

        return "list-donuts";
    }

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        // Trim whitespace from all string form parameters read by this controller
        // If the entire string is whitespace, trim it to null
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handle(Exception e) {
        e.printStackTrace();
    }
}
