package web.controlevacinacao.report;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;

@Component
public class JaspersoftUtil {

    private static final Logger logger = LoggerFactory.getLogger(JaspersoftUtil.class);

    public byte[] gerarRelatorio(String arquivoJasper, Map<String, Object> parametros, DataSource dataSource) {
        try (Connection conexao = dataSource.getConnection()) {
            try {
                ClassPathResource cpr = new ClassPathResource(arquivoJasper);
                InputStream arquivo = cpr.getInputStream();
                String urlRelatorio = cpr.getURL().toString();
                String diretorioRelatorios = urlRelatorio.substring(0, urlRelatorio.lastIndexOf("/") + 1);
                if (parametros == null) {
                    parametros = new HashMap<>();
                }
                parametros.put("SUBREPORT_DIR", diretorioRelatorios);
                JasperPrint jasperPrint = JasperFillManager.fillReport(arquivo, parametros, conexao);
                return JasperExportManager.exportReportToPdf(jasperPrint);
            } catch (JRException e) {
                logger.error("Problemas no Jasper na geracao do PDF do relatório: " + e);
            } catch (IOException e) {
                logger.error("Problemas nos arquivos de relatórios na geracao do PDF do relatório: " + e);
            }
        } catch (SQLException e) {
            logger.error("Problemas na obtenção de uma conexão com o BD na geração de relatório: " + e);
        }
        return null;
    }
}
