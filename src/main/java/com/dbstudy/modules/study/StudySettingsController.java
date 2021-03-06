package com.dbstudy.modules.study;

import com.dbstudy.modules.account.CurrentAccount;
import com.dbstudy.modules.account.Account;
import com.dbstudy.modules.tag.Tag;
import com.dbstudy.modules.zone.Zone;
import com.dbstudy.modules.study.form.StudyDescriptionForm;
import com.dbstudy.modules.study.form.TagForm;
import com.dbstudy.modules.study.form.ZoneForm;
import com.dbstudy.modules.tag.TagRepository;
import com.dbstudy.modules.tag.TagService;
import com.dbstudy.modules.zone.ZoneRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("study/{path}/settings")
@RequiredArgsConstructor
public class StudySettingsController {

    public static final String SETTINGS = "/settings";
    public static final String STUDYSETTINGS = "study/settings";
    public static final String DESCRIPTION = "/description";
    public static final String BANNER = "/banner";
    public static final String TAGS = "/tags";
    public static final String ZONES = "/zones";
    public static final String STUDY = "/study";

    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;
    private final StudyService studyService;
    private final TagService tagService;
    private final StudyRepository studyRepository;
    private final TagRepository tagRepository;
    private final ZoneRepository zoneRepository;

    @GetMapping(DESCRIPTION)
    public String viewStudySetting(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudyToUpdate(account, path);
        model.addAttribute(study);
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(study, StudyDescriptionForm.class));
        return STUDYSETTINGS + DESCRIPTION;
    }

    @PostMapping(DESCRIPTION)
    public String updateStudyInfo(@CurrentAccount Account account, @PathVariable String path,
                                  @Valid StudyDescriptionForm form, Errors errors,
                                  Model model, RedirectAttributes attributes) {
        Study study = studyService.getStudyToUpdate(account, path);

        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(study);
            return STUDYSETTINGS + DESCRIPTION;
        }
        studyService.updateStudyDescription(study, form);
        attributes.addFlashAttribute("message", "????????? ????????? ??????????????????.");
        return "redirect:/study/" + study.getEncodedPath() + SETTINGS + DESCRIPTION;
    }

    @GetMapping(BANNER)
    public String studyImageForm(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudyToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(study);
        return STUDYSETTINGS + BANNER;
    }

    @PostMapping(BANNER)
    public String studyImageSubmit(@CurrentAccount Account account, @PathVariable String path,
                                   String image, RedirectAttributes attributes) {
        Study study = studyService.getStudyToUpdate(account, path);
        studyService.updateStudyImage(study, image);
        attributes.addFlashAttribute("message", "????????? ???????????? ??????????????????.");
        return "redirect:/study/" + study.getEncodedPath() + SETTINGS + BANNER;
    }

    @PostMapping(BANNER + "/enable")
    public String enableStudyBanner(@CurrentAccount Account account, @PathVariable String path) {
        Study study = studyService.getStudyToUpdate(account, path);
        studyService.enableStudyBanner(study);
        return "redirect:/study/" + study.getEncodedPath() + SETTINGS + BANNER;
    }

    @PostMapping(BANNER + "/disable")
    public String disableStudyBanner(@CurrentAccount Account account, @PathVariable String path) {
        Study study = studyService.getStudyToUpdate(account, path);
        studyService.disableStudyBanner(study);
        return "redirect:/study/" + study.getEncodedPath() + SETTINGS + BANNER;
    }

    @GetMapping(TAGS)
    public String studyTagsForm(@CurrentAccount Account account, @PathVariable String path, Model model) throws JsonProcessingException {
        Study study = studyService.getStudyToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(study);

        model.addAttribute("tags", study.getTags().stream()
                .map(Tag::getTitle).collect(Collectors.toList()));

        List<String> allTags = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allTags));

        return STUDYSETTINGS + TAGS;
    }

    @PostMapping(TAGS + "/add")
    @ResponseBody
    public ResponseEntity addTag(@CurrentAccount Account account,@PathVariable String path,
                                 @RequestBody TagForm tagForm) {

        Study study = studyService.getStudyToUpdateTag(account, path);
        Tag tag = tagService.findOrCreateNew(tagForm.getTagTitle());
        studyService.addTag(study, tag);
        return ResponseEntity.ok().build();
    }

    @PostMapping(TAGS + "/remove")
    @ResponseBody
    public ResponseEntity removeTag(@CurrentAccount Account account, @PathVariable String path,
                                    @RequestBody TagForm tagForm) {

        Study study = studyService.getStudyToUpdateTag(account, path);
        Tag tag = tagRepository.findByTitle(tagForm.getTagTitle());
        if (tag == null) {
            return ResponseEntity.badRequest().build();
        }

        studyService.removeTag(study, tag);
        return ResponseEntity.ok().build();
    }

    @GetMapping(ZONES)
    public String studyZonesForm(@CurrentAccount Account account, @PathVariable String path, Model model) throws JsonProcessingException {
        Study study = studyService.getStudyToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(study);

        model.addAttribute("zones", study.getZones().stream()
                .map(Zone::toString).collect(Collectors.toList()));

        List<String> allZones = zoneRepository.findAll().stream().map(Zone::toString).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allZones));

        return STUDYSETTINGS + ZONES;
    }

    @PostMapping(ZONES + "/add")
    @ResponseBody
    public ResponseEntity addZone(@CurrentAccount Account account, @PathVariable String path,
                                  @RequestBody ZoneForm zoneForm) {
        Study study = studyService.getStudyToUpdateZone(account, path);
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if (zone == null) {
            return ResponseEntity.badRequest().build();
        }

        studyService.addZone(study, zone);
        return ResponseEntity.ok().build();
    }

    @PostMapping(ZONES + "/remove")
    @ResponseBody
    public ResponseEntity removeZone(@CurrentAccount Account account, @PathVariable String path,
                                  @RequestBody ZoneForm zoneForm) {
        Study study = studyService.getStudyToUpdateZone(account, path);
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if (zone == null) {
            return ResponseEntity.badRequest().build();
        }

        studyService.removeZone(study, zone);
        return ResponseEntity.ok().build();
    }

    @GetMapping(STUDY)
    public String studySettingForm(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudyToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(study);
        return STUDYSETTINGS + STUDY;
    }

    @PostMapping(STUDY + "/publish")
    public String studyPublish(@CurrentAccount Account account, @PathVariable String path, RedirectAttributes attributes) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        studyService.publish(study);
        attributes.addFlashAttribute("message", "???????????? ??????????????????.");
        return "redirect:/study/" + study.getEncodedPath() + SETTINGS + STUDY;
    }

    @PostMapping(STUDY + "/close")
    public String studyClose(@CurrentAccount Account account, @PathVariable String path, RedirectAttributes attributes) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        studyService.close(study);
        attributes.addFlashAttribute("message", "???????????? ??????????????????.");
        return "redirect:/study/" + study.getEncodedPath() + SETTINGS + STUDY;
    }

    @PostMapping("recruit/start")
    public String startRecruit(@CurrentAccount Account account, @PathVariable String path, RedirectAttributes attributes) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        if (!study.canUpdateRecruiting()) {
            attributes.addFlashAttribute("message", "1?????? ?????? ?????? ?????? ????????? ????????? ????????? ??? ????????????.");
            return "redirect:/study/" + study.getEncodedPath() + SETTINGS + STUDY;
        }
        studyService.startRecruiting(study);
        attributes.addFlashAttribute("message", "?????? ????????? ???????????????.");
        return "redirect:/study/" + study.getEncodedPath() + SETTINGS + STUDY;
    }

    @PostMapping("recruit/stop")
    public String stopRecruit(@CurrentAccount Account account, @PathVariable String path, RedirectAttributes attributes) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        if (!study.canUpdateRecruiting()) {
            attributes.addFlashAttribute("message", "1?????? ?????? ?????? ?????? ????????? ????????? ????????? ??? ????????????.");
            return "redirect:/study/" + study.getEncodedPath() + SETTINGS + STUDY;
        }
        studyService.stopRecruiting(study);
        attributes.addFlashAttribute("message", "?????? ????????? ???????????????.");
        return "redirect:/study/" + study.getEncodedPath() + SETTINGS + STUDY;
    }

    @PostMapping("study/path")
    public String updateStudyPath(@CurrentAccount Account account, @PathVariable String path, @RequestParam String newPath,
                                  Model model, RedirectAttributes attributes) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        if (!studyService.isValidPath(newPath)) {
            model.addAttribute(account);
            model.addAttribute(study);
            model.addAttribute("studyPathError", "?????? ????????? ????????? ????????? ??? ????????????. ?????? URL??? ???????????????.");
            return "study/settings/study";
        }
        studyService.updateStudyPath(study, newPath);
        attributes.addFlashAttribute("message", "????????? ????????? ??????????????????.");
        return "redirect:/study/" + study.getEncodedPath() + SETTINGS + STUDY;
    }

    @PostMapping("study/title")
    public String updateStudyTitle(@CurrentAccount Account account, @PathVariable String path, @RequestParam String newTitle,
                                   Model model, RedirectAttributes attributes) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        if (!studyService.isValidTitle(newTitle)) {
            model.addAttribute(account);
            model.addAttribute(study);
            model.addAttribute("studyTitleError", "?????? ????????? ????????? ????????? ??? ????????????. ?????? ????????? ???????????????.");
            return "study/settings/study";
        }
        studyService.updateStudyTitle(study, newTitle);
        attributes.addFlashAttribute("message", "????????? ????????? ??????????????????.");
        return "redirect:/study/" + study.getEncodedPath() + SETTINGS + STUDY;
    }

    @PostMapping("study/remove")
    public String removeStudy(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        studyService.remove(study);
        return "redirect:/";
    }
}
