/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.geant4.v2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import org.jlab.detector.volume.Geant4Basic;
import org.jlab.geometry.prim.Straight;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.jlab.detector.hits.DetHit;
import org.jlab.detector.volume.G4World;

/**
 *
 * @author kenjo
 */
public abstract class Geant4Factory {

    protected G4World motherVolume = new G4World("fc");
    protected final HashMap<String, String> properties = new HashMap<>();

    @Override
    public String toString() {
        return motherVolume.getChildren().stream()
                .map(child -> child.gemcStringRecursive())
                .collect(Collectors.joining());
    }

    public String getProperty(String name) {
        return properties.containsKey(name) ? properties.get(name) : "none";
    }

    public List<Geant4Basic> getComponents() {
        return motherVolume.getChildren().stream()
                .flatMap(child -> child.getComponents().stream())
                .collect(Collectors.toList());
    }

    public List<DetHit> getIntersections(Straight line) {
        return motherVolume.getChildren().stream()
                .flatMap(child -> child.getIntersections(line).stream())
                .collect(Collectors.toList());
    }

    public void toSCAD(String dirname, String scadname) throws IOException {
        File detdir = new File(dirname);
        detdir.mkdirs();
        try (PrintWriter scadout = new PrintWriter(new FileOutputStream(scadname, false))) {
            for (Geant4Basic component : getComponents()) {
                String stlpath = detdir.getAbsolutePath()+"/"+component.getName() + ".stl";
                System.out.println(stlpath);
                component.toCSG().toStlFile(stlpath);
                
                scadout.println(String.format("color([%d/255, %d/255, %d/255]) import(\"%s\");",
                        component.getColor()[0], component.getColor()[1], component.getColor()[2],
                        stlpath));
            }
        }
    }
}
