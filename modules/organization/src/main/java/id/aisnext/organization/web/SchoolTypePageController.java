package id.aisnext.organization.web;

import id.aisnext.organization.api.SchoolType;
import id.aisnext.organization.api.SchoolTypeCommand;
import id.aisnext.organization.api.SchoolTypeSort;
import id.aisnext.organization.application.SchoolTypeService;
import id.aisnext.organization.domain.SchoolTypeConflictException;
import id.aisnext.organization.domain.SchoolTypeNotFoundException;
import id.aisnext.organization.domain.SchoolTypeValidationException;
import id.aisnext.tenant.api.WriteOwnershipDeniedException;
import java.security.Principal;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Accessible server-rendered CRUD pages for the clone-gated school-type aggregate. */
@Controller
public class SchoolTypePageController {
    private final SchoolTypeService service;

    /**
     * Creates the page controller.
     *
     * @param service school-type application service
     */
    public SchoolTypePageController(SchoolTypeService service) {
        this.service = service;
    }

    /**
     * Renders the filterable, sortable, paginated catalogue.
     *
     * @param page zero-based page index
     * @param size requested page size
     * @param q case-insensitive name fragment
     * @param sort whitelisted sort name
     * @param activeOnly whether inactive rows are excluded
     * @param model model receiving page and preserved query state
     * @return school-type index template
     */
    @GetMapping("/school-types")
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "25") int size,
                       @RequestParam(defaultValue = "") String q,
                       @RequestParam(defaultValue = "NAME_ASC") String sort,
                       @RequestParam(defaultValue = "false") boolean activeOnly,
                       Model model) {
        SchoolTypeSort parsedSort = SchoolTypeSort.parse(sort);
        model.addAttribute("result", service.findAll(page, size, q, parsedSort, activeOnly));
        model.addAttribute("query", q);
        model.addAttribute("sort", parsedSort.name());
        model.addAttribute("activeOnly", activeOnly);
        return "school-types/index";
    }

    /**
     * Renders an empty create form and safe level choices.
     *
     * @param model model receiving null item and level choices
     * @return shared school-type form template
     */
    @GetMapping("/school-types/new")
    public String createForm(Model model) {
        populateForm(model, null);
        return "school-types/form";
    }

    /**
     * Renders an update form for one current row.
     *
     * @param id legacy primary key
     * @param model model receiving the row and level choices
     * @return form template or HTTP 404 when missing
     */
    @GetMapping("/school-types/{id}/edit")
    public Object updateForm(@PathVariable long id, Model model) {
        return service.findOne(id).<Object>map(value -> {
            populateForm(model, value);
            return "school-types/form";
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a school type and redirects to its filtered catalogue.
     *
     * @param name submitted name
     * @param levelId submitted education-level identifier
     * @param description optional description
     * @param active optional HTML checkbox value
     * @param principal authenticated handoff principal
     * @param redirect flash-message destination
     * @return redirect to the catalogue
     */
    @PostMapping("/school-types")
    public String create(@RequestParam String name, @RequestParam Long levelId,
                         @RequestParam(defaultValue = "") String description,
                         @RequestParam(defaultValue = "false") boolean active,
                         Principal principal, RedirectAttributes redirect) {
        try {
            SchoolType created = service.create(
                    new SchoolTypeCommand(name, levelId, description, active), principal.getName());
            redirect.addFlashAttribute("success", "Jenis sekolah " + created.name() + " berhasil ditambahkan");
        } catch (SchoolTypeValidationException | SchoolTypeConflictException
                 | WriteOwnershipDeniedException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/school-types";
    }

    /**
     * Updates a school type using the hidden snapshot token from the edit form.
     *
     * @param id legacy primary key
     * @param versionToken expected current snapshot token
     * @param name submitted name
     * @param levelId submitted education-level identifier
     * @param description optional description
     * @param active optional HTML checkbox value
     * @param principal authenticated handoff principal
     * @param redirect flash-message destination
     * @return redirect to the catalogue
     */
    @PostMapping("/school-types/{id}")
    public String update(@PathVariable long id, @RequestParam String versionToken,
                         @RequestParam String name, @RequestParam Long levelId,
                         @RequestParam(defaultValue = "") String description,
                         @RequestParam(defaultValue = "false") boolean active,
                         Principal principal, RedirectAttributes redirect) {
        try {
            SchoolType updated = service.update(id, versionToken,
                    new SchoolTypeCommand(name, levelId, description, active), principal.getName());
            redirect.addFlashAttribute("success", "Jenis sekolah " + updated.name() + " berhasil diperbarui");
        } catch (SchoolTypeValidationException | SchoolTypeConflictException
                 | SchoolTypeNotFoundException | WriteOwnershipDeniedException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/school-types";
    }

    /**
     * Deletes one current unreferenced row using its submitted snapshot token.
     *
     * @param id legacy primary key
     * @param versionToken expected current snapshot token
     * @param principal authenticated handoff principal
     * @param redirect flash-message destination
     * @return redirect to the catalogue
     */
    @PostMapping("/school-types/{id}/delete")
    public String delete(@PathVariable long id, @RequestParam String versionToken,
                         Principal principal, RedirectAttributes redirect) {
        try {
            service.delete(id, versionToken, principal.getName());
            redirect.addFlashAttribute("success", "Jenis sekolah berhasil dihapus");
        } catch (SchoolTypeConflictException | SchoolTypeNotFoundException
                 | WriteOwnershipDeniedException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/school-types";
    }

    /**
     * Populates shared form data without copying credential or unrelated tenant state.
     *
     * @param model target MVC model
     * @param item current row during edit, or null during create
     */
    private void populateForm(Model model, SchoolType item) {
        model.addAttribute("item", item);
        model.addAttribute("levels", service.selectableLevels());
    }
}
