package web.controlevacinacao.service;

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.stereotype.Service;
import web.controlevacinacao.report.JaspersoftUtil;

@Service
public class RelatoriosService {

    private DataSource dataSource;
    private JaspersoftUtil jaspersoftUtil;

    public RelatoriosService(DataSource dataSource, JaspersoftUtil jaspersoftUtil) {
        this.dataSource = dataSource;
        this.jaspersoftUtil = jaspersoftUtil;
    }

    public byte[] gerarRelatorioSimplesTodasVacinas() {
        return jaspersoftUtil.gerarRelatorio("/relatorios/RelatorioSQLDiretoSimples2.jasper", null, dataSource);
    }

    public byte[] gerarRelatorioComplexoTodasVacinasLotes() {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("TITULO", "Vacinas com Lotes");
        return jaspersoftUtil.gerarRelatorio("/relatorios/RelatorioSQLDiretoComplexoParametros2.jasper", parametros, dataSource);
    }
}
