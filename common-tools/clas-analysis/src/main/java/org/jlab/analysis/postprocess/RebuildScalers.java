package org.jlab.analysis.postprocess;

import java.sql.Time;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.detector.calib.utils.RCDBConstants;
import org.jlab.detector.decode.DaqScalers;
import org.jlab.detector.helicity.HelicitySequenceManager;
import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.io.HipoReader;
import org.jlab.jnp.hipo4.io.HipoWriterSorted;
import org.jlab.utils.groups.IndexedTable;
import org.jlab.utils.options.OptionParser;
import org.jlab.utils.system.ClasUtilsFile;

/**
 * Rebuild RUN::scaler from RAW::scaler
 * 
 * @author baltzell
 */
public class RebuildScalers {

    static final String CCDB_FCUP_TABLE="/runcontrol/fcup";
    static final String CCDB_SLM_TABLE="/runcontrol/slm";
    
    public static void main(String[] args) {
        
        OptionParser parser = new OptionParser("rebuildscaler");
        parser.addRequired("-o","output.hipo");
        parser.parse(args);
        List<String> inputList = parser.getInputList();
        if(inputList.isEmpty()==true){
            parser.printUsage();
            System.err.println("\n >>>> error : no input file is specified....\n");
            System.exit(1);
        }
        
        HelicitySequenceManager helSeq = new HelicitySequenceManager(8,inputList);
        
        HipoWriterSorted writer = new HipoWriterSorted();
        writer.getSchemaFactory().initFromDirectory(ClasUtilsFile.getResourceDir("COATJAVA", "etc/bankdefs/hipo4"));
        writer.setCompressionType(1);
        writer.open(parser.getOption("-o").stringValue());
			
        Event event = new Event();
        Bank rawScalerBank = new Bank(writer.getSchemaFactory().getSchema("RAW::scaler"));
        Bank runScalerBank = new Bank(writer.getSchemaFactory().getSchema("RUN::scaler"));
        Bank helScalerBank = new Bank(writer.getSchemaFactory().getSchema("HEL::scaler"));
        Bank runConfigBank = new Bank(writer.getSchemaFactory().getSchema("RUN::config"));
            
        ConstantsManager conman = new ConstantsManager();
        conman.init(Arrays.asList(new String[]{CCDB_FCUP_TABLE,CCDB_SLM_TABLE}));
        
        for (String filename : inputList) {

            HipoReader reader = new HipoReader();
            reader.open(filename);

            RCDBConstants rcdb = null;
            IndexedTable ccdb_fcup = null;
            IndexedTable ccdb_slm = null;

            while (reader.hasNext()) {

                // read the event and necessary banks:
                reader.nextEvent(event);
                event.read(runConfigBank);
                event.read(runScalerBank);
                event.read(helScalerBank);
                event.read(rawScalerBank);

                // this is the bank we're here to rebuild:
                event.remove(runScalerBank.getSchema());

                // get CCDB/RCDB constants:
                if (runConfigBank.getInt("run",0) >= 100) {
                    ccdb_fcup = conman.getConstants(runConfigBank.getInt("run",0),CCDB_FCUP_TABLE);
                    ccdb_slm = conman.getConstants(runConfigBank.getInt("run",0),CCDB_SLM_TABLE);
                    rcdb = conman.getRcdbConstants(runConfigBank.getInt("run",0));
                }

                // now rebuild the RUN::scaler bank: 
                if (rcdb!=null && ccdb_fcup !=null && rawScalerBank.getRows()>0) {
                    
                    // Run duration in seconds.  Nasty but works, until RCDB (uses java.sql.Time)
                    // is changed to support full date and not just HH:MM:SS.  Meanwhile just
                    // requires that runs last less than 24 hours.
                    Date uet = new Date(runConfigBank.getInt("unixtime",0)*1000L);
                    Time rst = rcdb.getTime("run_start_time");
                    final double s1 = rst.getSeconds()+60*rst.getMinutes()+60*60*rst.getHours();
                    final double s2 = uet.getSeconds()+60*uet.getMinutes()+60*60*uet.getHours();
                    final double seconds = s2<s1 ? s2+60*60*24-s1 : s2-s1;

                    DaqScalers ds = DaqScalers.create(rawScalerBank, ccdb_fcup, ccdb_slm, seconds);

                    // modify RUN::scaler:
                    runScalerBank.putFloat("fcupgated",0, (float) ds.dsc2.getBeamChargeGated());
                    runScalerBank.putFloat("fcup",0, (float) ds.dsc2.getBeamCharge());
                    runScalerBank.putFloat("livetime",0, (float) ds.dsc2.getLivetime());
                    
                    // modify HEL::scaler:
                    helScalerBank.putFloat("fcup",0, (float) ds.struck.getBeamCharge());
                    helScalerBank.putFloat("fcupgated",0, (float) ds.struck.getBeamChargeGated());
                    helScalerBank.putFloat("slm",0, (float) ds.struck.getBeamChargeSLM());
                    helScalerBank.putFloat("slmgated",0, (float) ds.struck.getBeamChargeGatedSLM());
                    helScalerBank.putFloat("clock",0,ds.struck.getClock());
                    helScalerBank.putFloat("clockgated",0,ds.struck.getGatedClock());
                    // the scaler banks always are delayed relative to helicity changes,
                    // so assign the previous helicity state to this scaler reading:
                    helScalerBank.putByte("helicity",0,helSeq.search(event,-1).value());
                    if (helSeq.getHalfWavePlate(event)) {
                        helScalerBank.putByte("helicityRaw",0,(byte)(-1*helSeq.search(event,-1).value()));
                    }
                    else {
                        helScalerBank.putByte("helicityRaw",0,helSeq.search(event,-1).value());
                    }


                    // put modified HEL/RUN::scaler back in the event:
                    event.write(runScalerBank);
                    event.write(helScalerBank);
                }

                writer.addEvent(event, event.getEventTag());
            }
            reader.close();
        }
        writer.close();
    }
}
