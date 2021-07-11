package com.dbstudy.study;

import com.dbstudy.account.CurrentAccount;
import com.dbstudy.domain.Account;
import com.dbstudy.domain.Study;
import com.dbstudy.study.form.StudyDescriptionForm;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;

@Controller
@RequestMapping("study/{path}/settings")
@RequiredArgsConstructor
public class StudySettingsController {

    public static final String SETTINGS = "/settings";
    public static final String STUDYSETTINGS = "study/settings";
    public static final String DESCRIPTION = "/description";
    public static final String BANNER = "/banner";

    private final StudyRepository studyRepository;
    private final StudyService studyService;
    private final ModelMapper modelMapper;

    @GetMapping(DESCRIPTION)
    public String viewStudySetting(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Study study = studyRepository.findByPath(path);
        model.addAttribute(study);
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(study, StudyDescriptionForm.class));
        return STUDYSETTINGS + DESCRIPTION;
    }

    @PostMapping(DESCRIPTION)
    public String updateStudyInfo(@CurrentAccount Account account, @PathVariable String path,
                                  @Valid StudyDescriptionForm form, Errors errors,
                                  Model model, RedirectAttributes attributes) throws AccessDeniedException {
        Study study = studyService.getStudyToUpdate(account, path);

        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(study);
            return STUDYSETTINGS + DESCRIPTION;
        }
        studyService.updateStudyDescription(study, form);
        attributes.addFlashAttribute("message", "스터디 소개를 수정했습니다.");
        return "redirect:/study/" + getPath(path) + SETTINGS + DESCRIPTION;
    }

    private String getPath(String path) {
        return URLEncoder.encode(path, StandardCharsets.UTF_8);
    }

    @GetMapping(BANNER)
    public String studyImageForm(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudyToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(study);
        return STUDYSETTINGS + BANNER;
    }

    @PostMapping("banner")
    public String studyImageSubmit(@CurrentAccount Account account, @PathVariable String path,
                                   String image, RedirectAttributes attributes) {
        Study study = studyService.getStudyToUpdate(account, path);
        studyService.updateStudyImage(study, image);
        attributes.addFlashAttribute("message", "스터디 이미지를 수정했습니다.");
        return "redirect:/study/" + getPath(path) + SETTINGS + BANNER;
    }

    @PostMapping(BANNER+"/enable")
    public String enableStudyBanner(@CurrentAccount Account account, @PathVariable String path) {
        Study study = studyService.getStudyToUpdate(account, path);
        studyService.enableStudyBanner(study);
        return "redirect:/study/" + getPath(path) + SETTINGS + BANNER;
    }

    @PostMapping(BANNER+"/disable")
    public String disableStudyBanner(@CurrentAccount Account account, @PathVariable String path) {
        Study study = studyService.getStudyToUpdate(account, path);
        studyService.disableStudyBanner(study);
        return "redirect:/study/" + getPath(path) + SETTINGS + BANNER;
    }

}
