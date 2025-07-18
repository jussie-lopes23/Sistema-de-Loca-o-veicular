package web.controlevacinacao.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxLocation;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import web.controlevacinacao.filter.VacinaFilter;
import web.controlevacinacao.model.Status;
import web.controlevacinacao.model.Vacina;
import web.controlevacinacao.notificacao.NotificacaoSweetAlert2;
import web.controlevacinacao.notificacao.TipoNotificaoSweetAlert2;
import web.controlevacinacao.pagination.PageWrapper;
import web.controlevacinacao.repository.VacinaRepository;
import web.controlevacinacao.service.VacinaService;

@Controller
public class VacinaController {

    private static final Logger logger = LoggerFactory.getLogger(VacinaController.class);

    private VacinaRepository vacinaRepository;
    private VacinaService vacinaService;

    public VacinaController(VacinaRepository vacinaRepository, VacinaService vacinaService) {
        this.vacinaRepository = vacinaRepository;
        this.vacinaService = vacinaService;
    }

    @HxRequest
    @GetMapping("/vacinas/abrirpesquisar")
    public String abrirPaginaPesquisaHTMX() {
        return "vacinas/pesquisar :: formulario";
    }

    @HxRequest
    @GetMapping("/vacinas/pesquisar")
    public String pesquisarHTMX(VacinaFilter filtro, Model model,
            @PageableDefault(size = 7) @SortDefault(sort = "codigo", direction = Sort.Direction.ASC) Pageable pageable,
            HttpServletRequest request) {
        Page<Vacina> pagina = vacinaRepository.pesquisar(filtro, pageable);
        logger.info("Vacinas pesquisadas: {}", pagina);
        PageWrapper<Vacina> paginaWrapper = new PageWrapper<>(pagina, request);
        model.addAttribute("pagina", paginaWrapper);
        return "vacinas/listar :: tabela";
    }

    @HxRequest
    @GetMapping("/vacinas/cadastrar")
    public String abrirCadastroHTMX(Vacina vacina) {
        return "vacinas/cadastrar :: formulario";
    }

    @HxRequest
    @PostMapping("/vacinas/cadastrar")
    public String cadastrarHTMX(@Valid Vacina vacina, BindingResult resultado,
            RedirectAttributes redirectAttributes) {
        if (resultado.hasErrors()) {
            logger.info("A vacina recebida para cadastrar não é válida.");
            logger.info("Erros encontrados:");
            for (FieldError erro : resultado.getFieldErrors()) {
                logger.info("{}", erro);
            }
            for (ObjectError erro : resultado.getGlobalErrors()) {
                logger.info("{}", erro);
            }
            return "vacinas/cadastrar :: formulario";
        } else {
            vacinaService.salvar(vacina);
            redirectAttributes.addFlashAttribute("notificacao", new NotificacaoSweetAlert2(
                    "Vacina cadastrada com sucesso!", TipoNotificaoSweetAlert2.SUCCESS, 4000));
            return "redirect:/vacinas/cadastrar";
        }
    }

    @HxRequest
    @GetMapping("/mensagem")
    public String mostrarMensagemHTMX(String mensagem, Model model) {
        if (mensagem != null && !mensagem.isEmpty()) {
            model.addAttribute("mensagem", mensagem);
        }
        return "mensagem :: texto";
    }

    @HxRequest
    @GetMapping("/vacinas/alterar/{codigo}")
    public String abrirAlterarHTMX(@PathVariable("codigo") Long codigo, Model model) {
        Vacina vacina = vacinaRepository.findByCodigoAndStatus(codigo, Status.ATIVO);
        if (vacina != null) {
            model.addAttribute("vacina", vacina);
            return "vacinas/alterar :: formulario";
        } else {
            model.addAttribute("mensagem", "Não existe uma vacina com esse código");
            return "mensagem :: texto";
        }
    }

    @HxRequest
    @PostMapping("/vacinas/alterar")
    public String alterarHTMX(@Valid Vacina vacina, BindingResult resultado,
            RedirectAttributes redirectAttributes) {
        if (resultado.hasErrors()) {
            logger.info("A vacina recebida para cadastrar não é válida.");
            logger.info("Erros encontrados:");
            for (FieldError erro : resultado.getFieldErrors()) {
                logger.info("{}", erro);
            }
            for (ObjectError erro : resultado.getGlobalErrors()) {
                logger.info("{}", erro);
            }
            return "vacinas/alterar :: formulario";
        } else {
            vacinaService.alterar(vacina);
            redirectAttributes.addFlashAttribute("notificacao", new NotificacaoSweetAlert2("Vacina alterada com sucesso!", TipoNotificaoSweetAlert2.SUCCESS, 4000));  // redirect
            return "redirect:/vacinas/abrirpesquisar";
        }
    }

    @HxRequest
    @HxLocation(path = "/mensagem", target = "#main", swap = "outerHTML")
    @GetMapping("/vacinas/remover/{codigo}")
    public String removerHTMX(@PathVariable("codigo") Long codigo, RedirectAttributes attributes) {
        vacinaService.remover(codigo);
        attributes.addFlashAttribute("notificacao",
                new NotificacaoSweetAlert2("Vacina removida com sucesso!", TipoNotificaoSweetAlert2.SUCCESS, 4000)); // redirect
        return "redirect:/vacinas/abrirpesquisar";
    }

    
}
