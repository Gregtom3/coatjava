/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.geant4.v2;

import eu.mihosoft.vrl.v3d.Trd;
import org.jlab.detector.units.SystemOfUnits.Length;

/**
 *
 * @author kenjo
 */
public class G4Trd extends Geant4Basic{
    
    public G4Trd(String name, double pdx1, double pdx2, double pdy1, double pdy2, double pdz) {
        super(name, "Trd", Length.unit(pdx1), Length.unit(pdx2), Length.unit(pdy1), Length.unit(pdy2), Length.unit(pdz));
        
        volumeSolid = new Trd(pdx1, pdx2, pdy1, pdy2, pdz);
    }
}
