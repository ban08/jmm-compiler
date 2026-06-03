package pt.up.fe.comp2026.backend;

import org.specs.comp.ollir.OllirErrorException;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;

/**
 * Implementation of the Jasmin backend.
 */
public class JasminBackendImpl implements JasminBackend {

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {

        //System.out.println("Converting OLLIR to Jasmin:\n" + ollirResult.getOllirCode());

        try {
            var classUnit = ollirResult.getOllirClass();
            classUnit.checkMethodLabels();
            classUnit.buildCFGs();
            classUnit.buildVarTables();
        } catch (OllirErrorException e) {
            var reports = new ArrayList<Report>();
            reports.add(Report.newError(Stage.BACKEND_GENERATION, -1, -1,
                    "Could not prepare OLLIR information for Jasmin generation", e));
            return new JasminResult(ollirResult, "", reports);
        }

        var jasminGenerator = new JasminGenerator(ollirResult);
        var jasminCode = jasminGenerator.build();

        //System.out.println("Generated Jasmin:\n" + jasminCode);

        return new JasminResult(ollirResult, jasminCode, jasminGenerator.getReports());
    }

}
