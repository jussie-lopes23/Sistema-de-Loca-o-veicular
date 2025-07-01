package web.controlevacinacao.controller;

import java.time.LocalDate;

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
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import web.controlevacinacao.filter.AplicacaoFilter;
import web.controlevacinacao.filter.LoteFilter;
import web.controlevacinacao.filter.PessoaFilter;
import web.controlevacinacao.model.Aplicacao;
import web.controlevacinacao.model.Lote;
import web.controlevacinacao.model.Pessoa;
import web.controlevacinacao.model.Status;
import web.controlevacinacao.notificacao.NotificacaoSweetAlert2;
import web.controlevacinacao.notificacao.TipoNotificaoSweetAlert2;
import web.controlevacinacao.pagination.PageWrapper;
import web.controlevacinacao.repository.AplicacaoRepository;
import web.controlevacinacao.repository.LoteRepository;
import web.controlevacinacao.repository.PessoaRepository;
import web.controlevacinacao.service.AplicacaoService;

@Controller
public class AplicacaoController {

    private static final Logger logger = LoggerFactory.getLogger(AplicacaoController.class);

    private PessoaRepository pessoaRepository;
    private LoteRepository loteRepository;
    private AplicacaoRepository aplicacaoRepository;
    private AplicacaoService aplicacaoService;

    public AplicacaoController(PessoaRepository pessoaRepository,
            LoteRepository loteRepository,
            AplicacaoService aplicacaoService,
            AplicacaoRepository aplicacaoRepository) {
        this.pessoaRepository = pessoaRepository;
        this.loteRepository = loteRepository;
        this.aplicacaoService = aplicacaoService;
        this.aplicacaoRepository = aplicacaoRepository;
    }

    @HxRequest
    @GetMapping("/aplicacoes/abrirpesquisarpessoa")
    public String abrirPaginaPesquisaPessoa() {
        return "aplicacoes/pesquisapessoa :: formulario";
    }

    @HxRequest
    @GetMapping("/aplicacoes/pesquisarpessoa")
    public String pesquisarPessoa(PessoaFilter filtro, Model model,
            @PageableDefault(size = 7) @SortDefault(sort = "codigo", direction = Sort.Direction.ASC) Pageable pageable,
            HttpServletRequest request) {
        Page<Pessoa> pagina = pessoaRepository.pesquisar(filtro, pageable);
        logger.info("Pessoas pesquisadas: {}", pagina.getContent());
        PageWrapper<Pessoa> paginaWrapper = new PageWrapper<>(pagina, request);
        model.addAttribute("pagina", paginaWrapper);
        return "aplicacoes/escolhapessoa :: tabela";
    }

    @HxRequest
    @GetMapping("/aplicacoes/abrirpesquisalote/{codigo}")
    public String abrirPesquisaLote(@PathVariable("codigo") Long codigo,
            Model model, HttpSession sessao) {
        Pessoa pessoa = pessoaRepository.findByCodigoAndStatus(codigo, Status.ATIVO);
        if (pessoa != null) {
            Aplicacao aplicacao = new Aplicacao();
            aplicacao.setPessoa(pessoa);
            sessao.setAttribute("aplicacao", aplicacao);
            return "aplicacoes/pesquisalote :: formulario";
        } else {
            model.addAttribute("mensagem", "Não existe uma pessoa com esse código");
            return "mensagem :: texto";
        }
    }

    @HxRequest
    @GetMapping("/aplicacoes/pesquisarlote")
    public String pesquisarLote(LoteFilter filtro, Model model,
            @PageableDefault(size = 7) @SortDefault(sort = "codigo", direction = Sort.Direction.ASC) Pageable pageable,
            HttpServletRequest request) {
        Page<Lote> pagina = loteRepository.pesquisar(filtro, pageable, true);
        logger.info("Lotes pesquisados: {}", pagina.getContent());
        PageWrapper<Lote> paginaWrapper = new PageWrapper<>(pagina, request);
        model.addAttribute("pagina", paginaWrapper);
        return "aplicacoes/escolhalote:: tabela";
    }

    // /aplicacoes/abrircadastro/{codigo}
    @HxRequest
    @GetMapping("/aplicacoes/abrircadastro/{codigo}")
    public String abrirCadastro(@PathVariable("codigo") Long codigo,
            Model model, HttpSession sessao, Aplicacao aplicacao) {
        Lote lote = loteRepository.findByCodigoAndStatus(codigo, Status.ATIVO);
        if (lote != null) {
            aplicacao = (Aplicacao) sessao.getAttribute("aplicacao");
            aplicacao.setLote(lote);
            aplicacao.setData(LocalDate.now());
            model.addAttribute("aplicacao", aplicacao);
            sessao.setAttribute("aplicacao", aplicacao);
            return "aplicacoes/cadastrar :: formulario";
        } else {
            model.addAttribute("mensagem", "Não existe um lote com esse código");
            return "mensagem :: texto";
        }
    }

    @HxRequest
    @PostMapping("/aplicacoes/cadastrar")
    public String cadastrar(@Valid Aplicacao aplicacao, BindingResult resultado,
            RedirectAttributes redirectAttributes, HttpSession sessao, Model model) {
        if (resultado.hasErrors()) {
            logger.info("A aplicação recebida para cadastrar não é válida.");
            logger.info("Erros encontrados:");
            for (FieldError erro : resultado.getFieldErrors()) {
                logger.info("{}", erro);
            }
            for (ObjectError erro : resultado.getGlobalErrors()) {
                logger.info("{}", erro);
            }
            model.addAttribute("aplicacao", aplicacao);
            return "aplicacoes/cadastrar :: formulario";
        } else {
            Aplicacao salva = (Aplicacao) sessao.getAttribute("aplicacao");
            salva.setData(aplicacao.getData());
            aplicacaoService.salvar(salva);
            sessao.removeAttribute("aplicacao");
            redirectAttributes.addFlashAttribute("notificacao", new NotificacaoSweetAlert2(
                    "Aplicação cadastrada com sucesso!", TipoNotificaoSweetAlert2.SUCCESS, 4000));
            return "redirect:/aplicacoes/abrirpesquisarpessoa";
        }
    }

    @HxRequest
    @GetMapping("/aplicacoes/abrirpesquisar")
    public String abrirPaginaPesquisa() {
        return "aplicacoes/pesquisa :: formulario";
    }

    @HxRequest
    @GetMapping("/aplicacoes/pesquisar")
    public String pesquisar(AplicacaoFilter filtro, Model model,
            @PageableDefault(size = 7) @SortDefault(sort = "codigo", direction = Sort.Direction.ASC) Pageable pageable,
            HttpServletRequest request) {
        Page<Aplicacao> pagina = aplicacaoRepository.pesquisar(filtro, pageable);
        logger.info("Aplicações pesquisadas: {}", pagina.getContent());
        PageWrapper<Aplicacao> paginaWrapper = new PageWrapper<>(pagina, request);
        model.addAttribute("pagina", paginaWrapper);
        return "aplicacoes/listar :: tabela";
    }

    @HxRequest
    @HxLocation(path = "/mensagem", target = "#main", swap = "outerHTML")
    @GetMapping("/aplicacoes/remover/{codigo}")
    public String remover(@PathVariable("codigo") Long codigo, RedirectAttributes attributes) {
        aplicacaoService.desativar(codigo);
        attributes.addFlashAttribute("notificacao",
                new NotificacaoSweetAlert2("Aplicação removida com sucesso!", TipoNotificaoSweetAlert2.SUCCESS, 4000)); // redirect
        return "redirect:/aplicacoes/abrirpesquisar";
    }

    // /aplicacoes/alterar/{codigo}
    @HxRequest
    @GetMapping("/aplicacoes/alterar/{codigo}")
    public String abrirAlterar(@PathVariable("codigo") Long codigo, Model model) {
        Aplicacao aplicacao = aplicacaoRepository.buscar(codigo);
        if (aplicacao != null) {
            model.addAttribute("aplicacao", aplicacao);
            return "aplicacoes/alterar :: formulario";
        } else {
            model.addAttribute("mensagem", "Não existe uma aplicação com esse código");
            return "mensagem :: texto";
        }
    }

    @HxRequest
    @PostMapping("/aplicacoes/alterar")
    public String alterar(@Valid Aplicacao aplicacao, BindingResult resultado,
            RedirectAttributes redirectAttributes) {
        if (resultado.hasErrors()) {
            logger.info("A aplicação recebida para cadastrar não é válida.");
            logger.info("Erros encontrados:");
            for (FieldError erro : resultado.getFieldErrors()) {
                logger.info("{}", erro);
            }
            for (ObjectError erro : resultado.getGlobalErrors()) {
                logger.info("{}", erro);
            }
            return "aplicacoes/alterar :: formulario";
        } else {
            Aplicacao salva = aplicacaoRepository.buscar(aplicacao.getCodigo());
            salva.setData(aplicacao.getData());
            aplicacaoService.alterar(salva);
            redirectAttributes.addFlashAttribute("notificacao",
                    new NotificacaoSweetAlert2("Aplicação alterada com sucesso!", TipoNotificaoSweetAlert2.SUCCESS, 4000)); // redirect
            return "redirect:/aplicacoes/abrirpesquisar";
        }
    }
}
