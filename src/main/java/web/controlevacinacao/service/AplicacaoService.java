package web.controlevacinacao.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import web.controlevacinacao.model.Aplicacao;
import web.controlevacinacao.model.Lote;
import web.controlevacinacao.model.Status;
import web.controlevacinacao.repository.AplicacaoRepository;

@Service
@Transactional
public class AplicacaoService {
    private AplicacaoRepository aplicacaoRepository;
    private LoteService loteService;

    public AplicacaoService(AplicacaoRepository aplicacaoRepository,
            LoteService loteService) {
        this.aplicacaoRepository = aplicacaoRepository;
        this.loteService = loteService;
    }

    public void salvar(Aplicacao aplicacao) {
        aplicacaoRepository.save(aplicacao);
        Lote lote = aplicacao.getLote();
        lote.setNroDosesAtual(lote.getNroDosesAtual() - 1);
        loteService.alterar(lote);
    }

    public void alterar(Aplicacao aplicacao) {
        aplicacaoRepository.save(aplicacao);
    }

    public void remover(Long codigo) {
        aplicacaoRepository.deleteById(codigo);
    }

    public void desativar(Long codigo) {
        Aplicacao aplicacao = aplicacaoRepository.findById(codigo).get();
        aplicacao.setStatus(Status.INATIVO);
        aplicacaoRepository.save(aplicacao);
    }
}
