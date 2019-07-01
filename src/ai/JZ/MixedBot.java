
package ai.JZ;

import Standard.ImprovedStrategyTactics;
import ai.abstraction.AbstractionLayerAI;
import ai.abstraction.partialobservability.POLightRush;
import ai.abstraction.pathfinding.AStarPathFinding;
import ai.abstraction.pathfinding.PathFinding;
import ai.competition.capivara.ImprovedCapivara;
import ai.competition.tiamat.ImprovedTiamat;
import ai.configurablescript.BasicExpandedConfigurableScript;
import ai.configurablescript.ScriptsCreator;
import ai.core.AI;
import ai.core.ParameterSpecification;
import ai.evaluation.EvaluationFunction;
import ai.evaluation.SimpleSqrtEvaluationFunction3;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;

/**
 *
 * @author smoyzsh
 * @author analysis.jinger
 */

public class MixedBot extends AbstractionLayerAI {
    int LOOKAHEAD = 200;
    int I = 0;
    int R = 0;
    GameState gs_to_start_from = null;
    int playerForThisComputation;
    long start_time = 0;
    boolean started = false;
    ImprovedStrategyTactics baseAI = null;
    UnitTypeTable utt;
    ScriptsCreator sc;
    ArrayList<BasicExpandedConfigurableScript> scriptsCompleteSet;

    public MixedBot(UnitTypeTable utt){
        super(new AStarPathFinding(),100, 200);
        this.utt = utt;
        sc = new ScriptsCreator(utt,300);
        scriptsCompleteSet = sc.getScriptsMixReducedSet();
    }

    public MixedBot(int time, int max_playouts) {
        super(new AStarPathFinding(),time, max_playouts);
        started = false;
    }

    @Override
    public void reset() {
        this.started  = false;
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
        return new MixedBot(utt);
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


    public void computeDuringOneGameFrame() throws Exception {

    }


    public PlayerAction getBestActionSoFar() throws Exception {
        return baseAI.getAction(playerForThisComputation, gs_to_start_from);
    }

    private void inicializeAI(GameState gs) throws Exception {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        if(pgs.getHeight() == 8 && pgs.getWidth() == 8){
            this.baseAI = new ImprovedStrategyTactics(utt,30,70,new ImprovedTiamat(utt), new ImprovedCapivara(utt), 0);
        }
        else if(pgs.getHeight() == 8 && pgs.getWidth() == 9){
            this.baseAI = new ImprovedStrategyTactics(utt,60,40,new ImprovedTiamat(utt), new ImprovedCapivara(utt), 0);
        }
        else if( (pgs.getHeight() >= 9 && pgs.getHeight() <= 16) && (pgs.getWidth() >= 9 && pgs.getWidth() <= 16) ){
            this.baseAI = new ImprovedStrategyTactics(utt,40,60,new ImprovedTiamat(utt), new ImprovedCapivara(utt), 10);
        }
        else if( (pgs.getHeight() > 16 && pgs.getHeight() <= 24) && (pgs.getWidth() > 16 && pgs.getWidth() <= 24) ){
            this.baseAI = new ImprovedStrategyTactics(utt,80,20,new ImprovedTiamat(utt), new ImprovedCapivara(utt), 10);
        }else if( (pgs.getHeight() > 24 && pgs.getHeight() <= 32) && (pgs.getWidth() > 24 && pgs.getWidth() <= 32) ){
            this.baseAI = new ImprovedStrategyTactics(utt,60,40,new ImprovedTiamat(utt), new ImprovedCapivara(utt), 0);

        }
        else{
            this.baseAI = new ImprovedStrategyTactics(utt,20,80,new ImprovedTiamat(utt), new ImprovedCapivara(utt), 5);
            this.baseAI.setTimeBudget(60);
        }
    }

    @Override
    public void preGameAnalysis(GameState gs, long milliseconds) throws Exception
    {   inicializeAI(gs);
        started = true;
        this.baseAI.preGameAnalysis(gs, milliseconds);
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

    public PathFinding getPathFinding() {
        return pf;
    }

    public void setPathFinding(PathFinding a_pf) {
        pf = a_pf;
    }
}
