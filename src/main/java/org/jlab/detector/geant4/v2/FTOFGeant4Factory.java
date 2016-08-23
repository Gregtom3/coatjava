/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.geant4.v2;

import java.util.ArrayList;
import java.util.List;
import static org.jlab.detector.geant4.v2.SystemOfUnits.Length;
import org.jlab.geom.base.ConstantProvider;

/**
 *
 * @author gavalian, kenjo
 */
public final class FTOFGeant4Factory extends Geant4Factory {

    private final String[] stringLayers = new String[]{
        "/geometry/ftof/panel1a",
        "/geometry/ftof/panel1b",
        "/geometry/ftof/panel2"};

    private final String[] gemcLayerNames = new String[]{
        "1a", "1b", "2"
    };

    public FTOFGeant4Factory(ConstantProvider provider) {
        motherVolume = new G4Box("fc", 0, 0, 0);
        
        for (int sector = 1; sector <= 6; sector++) {
            for (int layer = 1; layer <= 3; layer++) {
                Geant4Basic layerVolume = createPanel(provider, sector, layer);
                layerVolume.setMother(motherVolume);
            }
        }
        properties.put("email", "carman@jlab.org, jguerra@jlab.org");
        properties.put("author", "carman, guerra");
        properties.put("date", "06/03/13");
    }

    public Geant4Basic createPanel(ConstantProvider cp, int sector, int layer) {
        double motherGap = 4.0*Length.cm;

        double thtilt = Math.toRadians(cp.getDouble(stringLayers[layer - 1] + "/panel/thtilt", 0));
        double thmin = Math.toRadians(cp.getDouble(stringLayers[layer - 1] + "/panel/thmin", 0));
        double dist2edge = cp.getDouble(stringLayers[layer - 1] + "/panel/dist2edge", 0)*Length.cm;

        List<G4Box> paddles = this.createLayer(cp, layer);

        double panel_width = (paddles.get(paddles.size() - 1).getPosition().z- paddles.get(0).getPosition().z)
                + 2 * paddles.get(0).getZHalfLength() + 2 * motherGap;

        double panel_mother_dx1 = paddles.get(0).getXHalfLength() + motherGap;
        double panel_mother_dx2 = paddles.get(paddles.size() - 1).getXHalfLength()
                + (paddles.get(paddles.size() - 1).getXHalfLength() - paddles.get(paddles.size() - 2).getXHalfLength())
                + motherGap;

        double panel_mother_dy = paddles.get(0).getYHalfLength() + motherGap;
        double panel_mother_dz = panel_width / 2.0;

        Geant4Basic panelVolume = new G4Trd("ftof_p" + gemcLayerNames[layer - 1] + "_s" + sector,
                panel_mother_dx1, panel_mother_dx2, panel_mother_dy, panel_mother_dy, panel_mother_dz);
        panelVolume.setId(sector, layer, 0);

        double panel_pos_xy = dist2edge * Math.sin(thmin) + panel_width / 2 * Math.cos(thtilt);
        double panel_pos_x = panel_pos_xy * Math.cos(Math.toRadians(sector * 60 - 60));
        double panel_pos_y = panel_pos_xy * Math.sin(Math.toRadians(sector * 60 - 60));
        double panel_pos_z = dist2edge * Math.cos(thmin) - panel_width / 2 * Math.sin(thtilt);

        panelVolume.setPosition(panel_pos_x, panel_pos_y, panel_pos_z);
        panelVolume.setRotation("xyz", Math.toRadians(-90) - thtilt, 0.0, Math.toRadians(-30.0 - sector * 60.0));

        for (int ipaddle = 0; ipaddle < paddles.size(); ipaddle++) {
            paddles.get(ipaddle).setName("panel" + gemcLayerNames[layer - 1] + "_sector" + sector + "_paddle_" + (ipaddle + 1));
            paddles.get(ipaddle).setId(sector, layer, ipaddle + 1);

            paddles.get(ipaddle).setMother(panelVolume);
        }
        return panelVolume;
    }

    public List<G4Box> createLayer(ConstantProvider cp, int layer) {

        int numPaddles = cp.length(stringLayers[layer - 1] + "/paddles/paddle");
        double paddlewidth = cp.getDouble(stringLayers[layer - 1] + "/panel/paddlewidth", 0);
        double paddlethickness = cp.getDouble(stringLayers[layer - 1] + "/panel/paddlethickness", 0);
        double gap = cp.getDouble(stringLayers[layer - 1] + "/panel/gap", 0);
        double wrapperthickness = cp.getDouble(stringLayers[layer - 1] + "/panel/wrapperthickness", 0);
        double thtilt = Math.toRadians(cp.getDouble(stringLayers[layer - 1] + "/panel/thtilt", 0));
        double thmin = Math.toRadians(cp.getDouble(stringLayers[layer - 1] + "/panel/thmin", 0));

        String paddleLengthStr = stringLayers[layer - 1] + "/paddles/Length";

        List<G4Box> paddleVolumes = new ArrayList<>();

        for (int ipaddle = 0; ipaddle < numPaddles; ipaddle++) {
            double paddlelength = cp.getDouble(paddleLengthStr, ipaddle);
            String vname = String.format("sci_S%d_L%d_C%d", 0, layer, ipaddle + 1);
            G4Box volume = new G4Box(vname, paddlelength / 2.*Length.cm, paddlethickness / 2.*Length.cm, paddlewidth / 2.0*Length.cm);

            double zoffset = (ipaddle - numPaddles / 2. + 0.5) * (paddlewidth + gap + 2 * wrapperthickness);
            volume.setPosition(0.0, 0.0, zoffset*Length.cm);
            volume.setRotation("xyz", 0, 0, 0);
            
            paddleVolumes.add(volume);
        }
        return paddleVolumes;
    }

}
