/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.competition.tiamat;

import ai.abstraction.AbstractionLayerAI;
import ai.abstraction.partialobservability.POHeavyRush;
import ai.abstraction.partialobservability.POLightRush;
import ai.abstraction.partialobservability.PORangedRush;
import ai.abstraction.partialobservability.POWorkerRush;
import ai.abstraction.pathfinding.AStarPathFinding;
import ai.abstraction.pathfinding.PathFinding;
import ai.asymmetric.PGS.EditedPGSmRTS;
import ai.asymmetric.SSS.EditedSSSmRTS;
import ai.configurablescript.BasicExpandedConfigurableScript;
import ai.configurablescript.ScriptsCreator;
import ai.core.AI;
import ai.core.AIWithComputationBudget;
import ai.core.ParameterSpecification;
import ai.evaluation.EvaluationFunction;
import ai.evaluation.SimpleSqrtEvaluationFunction3;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;

/**
 *
 * @author Julian, Rubens and Levi
 * edited by smoyzsh and jinger
 */
public class ImprovedTiamat extends AbstractionLayerAI {
    int LOOKAHEAD = 200;
    int I = 0;
    int R = 0;
    EvaluationFunction evaluation = null;
    PathFinding pf;
    GameState gs_to_start_from = null;
    int playerForThisComputation;
    long start_time = 0;
    boolean started = false;
    AIWithComputationBudget baseAI = null;
    UnitTypeTable utt;
    ScriptsCreator sc;
    ArrayList<BasicExpandedConfigurableScript> scriptsCompleteSet;

    public ImprovedTiamat(UnitTypeTable utt){
        super(new AStarPathFinding(),100, 200);
        this.utt = utt;
        sc = new ScriptsCreator(utt,300);
        scriptsCompleteSet = sc.getScriptsMixReducedSet();
    }

    public ImprovedTiamat(int time, int max_playouts) {
        super(new AStarPathFinding(),time, max_playouts);
        started = false;
    }

    @Override
    public void reset() {
        this.started = false;
    }

    @Override
    public PlayerAction getAction(int player, GameState gs) throws Exception {
        if (gs.canExecuteAnyAction(player)) {
            if(!started){
                inicializeAI(gs);
                started = true;
            }
            startNewComputation(player, gs);
            return getBestActionSoFar();
        } else {
            return new PlayerAction();
        }
    }

    @Override
    public AI clone() {
        return new ImprovedTiamat(utt);
    }

    @Override
    public List<ParameterSpecification> getParameters() {
        List<ParameterSpecification> parameters = new ArrayList<>();

        parameters.add(new ParameterSpecification("TimeBudget", int.class, this.TIME_BUDGET));
        parameters.add(new ParameterSpecification("IterationsBudget", int.class, -1));
        parameters.add(new ParameterSpecification("PlayoutLookahead", int.class, this.ITERATIONS_BUDGET));
        parameters.add(new ParameterSpecification("EvaluationFunction", EvaluationFunction.class, new SimpleSqrtEvaluationFunction3()));
        parameters.add(new ParameterSpecification("PathFinding", PathFinding.class, new AStarPathFinding()));

        return parameters;
    }

    public void startNewComputation(int player, GameState gs) throws Exception {
        playerForThisComputation = player;
        gs_to_start_from = gs;
        start_time = System.currentTimeMillis();
    }
    @Override
    public void setTimeBudget(int time){
        TIME_BUDGET = time;
        if(this.baseAI != null){
            this.baseAI.setTimeBudget(time);
        }

    }
    public PlayerAction getBestActionSoFar() throws Exception {
        if(!started){
            inicializeAI(gs_to_start_from);
            started = true;
            return  new PlayerAction();
        }
        return baseAI.getAction(playerForThisComputation, gs_to_start_from);
    }
    @Override
    public void preGameAnalysis(GameState gs, long milliseconds) throws Exception
    {   started = true;
        this.inicializeAI(gs);
    }
    private void inicializeAI(GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        List<AI> ai_list = new ArrayList<>();
        if(pgs.getHeight() == 8 && pgs.getWidth() == 8){
            ai_list = decodeScripts(utt, "48;0;");
            ai_list.add(new POWorkerRush(utt));
            this.baseAI = new EditedSSSmRTS(utt, ai_list, "Tiamat",4,200);
        } else if(pgs.getHeight() == 8 && pgs.getWidth() == 9){
            this.baseAI = new EditedPGSmRTS(utt, decodeScripts(utt, "203;3;"), "Tiamat",4,200);
        } else if( (pgs.getHeight() >= 9 && pgs.getHeight() <= 16) && (pgs.getWidth() >= 9 && pgs.getWidth() <= 16) ){
            ai_list = decodeScripts(utt, "1;270;");
//            ai_list.add(new POWorkerRush(utt));
            this.baseAI = new EditedSSSmRTS(utt, ai_list, "Tiamat",4,200);
        } else if( (pgs.getHeight() > 16 && pgs.getHeight() <= 24) && (pgs.getWidth() > 16 && pgs.getWidth() <= 24) ){
            this.baseAI = new EditedPGSmRTS(utt, decodeScripts(utt, "93;45;264;211;42;"), "Tiamat",4,200);
        }else if( (pgs.getHeight() > 24 && pgs.getHeight() <= 32) && (pgs.getWidth() > 24 && pgs.getWidth() <= 32) ){
            ai_list = decodeScripts(utt, "1;285;244;272;198;");
            this.baseAI = new EditedSSSmRTS(utt,ai_list , "Tiamat",4,200);

        }else{
            this.baseAI = new EditedSSSmRTS(utt, decodeScripts(utt, "297;158;256;"), "Tiamat",4,200);
        }
    }

    public void computeDuringOneGameFrame() throws Exception {

    }

    private List<AI> decodeScripts(UnitTypeTable utt, String sScripts) {
        ArrayList<Integer> iScriptsAi1 = new ArrayList<>();
        String[] itens = sScripts.split(";");
        for (String element : itens) {
            iScriptsAi1.add(Integer.decode(element));
        }
        List<AI> scriptsAI = new ArrayList<>();
        iScriptsAi1.forEach((idSc) -> {
            scriptsAI.add(scriptsCompleteSet.get(idSc));
        });
        return scriptsAI;
    }

    public int getPlayoutLookahead() {
        return LOOKAHEAD;
    }

    public void setPlayoutLookahead(int a_pola) {
        LOOKAHEAD = a_pola;
    }

    public int getI() {
        return I;
    }

    public void setI(int a) {
        I = a;
    }

    public int getR() {
        return R;
    }

    public void setR(int a) {
        R = a;
    }

    public EvaluationFunction getEvaluationFunction() {
        return evaluation;
    }

    public void setEvaluationFunction(EvaluationFunction a_ef) {
        evaluation = a_ef;
    }

    public PathFinding getPathFinding() {
        return pf;
    }

    public void setPathFinding(PathFinding a_pf) {
        pf = a_pf;
    }
}
