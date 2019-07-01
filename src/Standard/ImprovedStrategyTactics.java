package Standard;

import java.util.ArrayList;
import java.util.List;

import ai.competition.capivara.ImprovedCapivara;
import ai.competition.tiamat.ImprovedTiamat;
import ai.core.AI;
import ai.core.AIWithComputationBudget;
import ai.core.InterruptibleAI;
import ai.core.ParameterSpecification;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.ResourceUsage;
import rts.UnitAction;
import rts.units.Unit;
import rts.units.UnitTypeTable;
import util.Pair;

public class ImprovedStrategyTactics extends AIWithComputationBudget implements InterruptibleAI {

    ImprovedTiamat strategyAI;
    ImprovedCapivara tacticsAI;
    boolean start;
    protected int DEBUG = 0;
    int weightStrategy, weightTactics;
    int origTimeBudget, origItBudget;
    int decay_base = 0;
    public ImprovedStrategyTactics(UnitTypeTable utt,int weightStrategy, int weightTactics, ImprovedTiamat strategyAI, ImprovedCapivara tacticsAI, int decay_base) throws Exception {
        super(100,-1);
        origTimeBudget = 100;
        origItBudget = -1;
        this.strategyAI = strategyAI;
        this.tacticsAI = tacticsAI;
        this.weightStrategy = weightStrategy;
        this.weightTactics = weightTactics;
        this.decay_base = decay_base;
        start = false;
    }


    @Override
    public void reset() {
        strategyAI.reset();
        tacticsAI.reset();
    }

    @Override
    public PlayerAction getAction(int player, GameState gs) throws Exception {
        //System.out.println("Warning: not using contnuingAI!!!");
        if (gs.canExecuteAnyAction(player)) {
            if(!start){
                this.preGameAnalysis(gs,100);
                start = true;
            }
            startNewComputation(player, gs.clone());
            computeDuringOneGameFrame();
            return getBestActionSoFar();
        } else {
            return new PlayerAction();
        }
    }

    @Override
    public AI clone() {
        System.out.println("use clone");
        return null;

    }

    @Override
    public List<ParameterSpecification> getParameters() {
        List<ParameterSpecification> parameters = new ArrayList<>();
        return parameters;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "(" + strategyAI.getClass().getSimpleName()
                + ", " + tacticsAI.getClass().getSimpleName() + ")";
    }
    //ReducedGameState _rgs;
    GameState _gs;

    @Override
    public void startNewComputation(int player, GameState gs) throws Exception {
        if (DEBUG >= 2) {
            System.out.println("start");
        }

        _gs = gs.clone();
        ReducedGameState rgs = new ReducedGameState(_gs);

        assert (_gs.getTime() == rgs.getTime());
        //if(DEBUG>=1)System.out.println("Frame: "+gs.getTime()+" original size: "+gs.getUnits().size()+", reduced size: "+rgs.getUnits().size());
        boolean p0 = false, p1 = false;
        int attack_unit_cnt = 0;
        for (Unit u : rgs.getUnits()) {
            if (u.getPlayer() == 0) {
                p0 = true;
            }
            if (u.getPlayer() == 1) {
                p1 = true;
            }
            if( u.getType().canAttack){
                attack_unit_cnt += 1;
            }
        }
        //System.out.println("attack_unit_cnt : " + attack_unit_cnt);
        int dynamic_weightStrategy;
        int dynamic_weightTactics;
        if(this.decay_base == 0){
            dynamic_weightStrategy = weightStrategy;
            dynamic_weightTactics = weightTactics;
        } else {
            double weight_decay = 1.0 * attack_unit_cnt / this.decay_base;
            dynamic_weightStrategy = (int) (100 * (1- weight_decay));
            if ( dynamic_weightStrategy < weightStrategy ){
                dynamic_weightStrategy = weightStrategy;
            }
            dynamic_weightTactics = (int)(100 * weight_decay);
            if( dynamic_weightTactics > weightTactics){
                dynamic_weightTactics = weightTactics;
            }
        }



//        System.out.println("dynamic_weightStrategy : " + dynamic_weightStrategy + " dynamic_weightTactics " + dynamic_weightTactics);


        if (!(p0 && p1) || !rgs.canExecuteAnyAction(player)) {
            if (DEBUG >= 1) {
                System.out.println("Strategy only");
            }
            strategyAI.setTimeBudget(TIME_BUDGET - 10);
//            System.out.println("stategy time " + (TIME_BUDGET - 10));
            strategyAI.startNewComputation(player, _gs);
            tacticsAI.setTimeBudget(0);
        } else {
            //int strategyTime = TIME_BUDGET * weightStrategy / (weightStrategy + weightTactics);
            int strategyTime = TIME_BUDGET * dynamic_weightStrategy / (dynamic_weightStrategy + dynamic_weightTactics) - 5;
//            System.out.println("stategy time " + strategyTime);
            strategyAI.setTimeBudget(strategyTime);
            strategyAI.startNewComputation(player, _gs);

            int tacticsTime = TIME_BUDGET * dynamic_weightTactics / (dynamic_weightStrategy + dynamic_weightTactics) - 5;
//            System.out.println("tactics Time " + tacticsTime);
            if(tacticsTime < 0){
                tacticsTime = 0;
            }
            tacticsAI.setTimeBudget(tacticsTime);
            tacticsAI.startNewComputation(player, rgs);
        }
    }

    @Override
    public void computeDuringOneGameFrame() throws Exception {
        if (DEBUG >= 2) {
            System.out.println("think strategy");
        }
        if (tacticsAI.getTimeBudget() > 0) {
            if (DEBUG >= 2) {
                System.out.println("think tactics");
                tacticsAI.computeDuringOneGameFrame();
            }
        }
    }
    @Override
    public void preGameAnalysis(GameState gs, long milliseconds) throws Exception
    {
        this.strategyAI.preGameAnalysis(gs,milliseconds);
        this.tacticsAI.preGameAnalysis(gs, milliseconds);
    }
    @Override
    public PlayerAction getBestActionSoFar() throws Exception {
        if (DEBUG >= 2) {
            System.out.println("get");
        }
        long start_time;
        long end_time;
        if (tacticsAI.getTimeBudget() <= 0) {
            PlayerAction paStrategy = strategyAI.getBestActionSoFar();
            return paStrategy;
        } else {
            start_time = System.currentTimeMillis();
            PlayerAction paStrategy = strategyAI.getBestActionSoFar();
            end_time = System.currentTimeMillis();
//            System.out.println("actual strategy time is " + (end_time - start_time));

            start_time = System.currentTimeMillis();
            PlayerAction paTactics = tacticsAI.getBestActionSoFar();
            end_time = System.currentTimeMillis();
//            System.out.println("actual Tactics time is " + (end_time - start_time));
            //System.out.println("Extra search with "+rgs.getUnits().size()+" units");
            if (DEBUG >= 1) {
                System.out.println("actions: " + paTactics.getActions());
            }

            //remove non attacking units
            List<Pair<Unit, UnitAction>> toRemove = new ArrayList<Pair<Unit, UnitAction>>();
            for (Pair<Unit, UnitAction> ua : paTactics.getActions()) {
                if (!ua.m_a.getType().canAttack
                        || ua.m_b.getType() == UnitAction.TYPE_PRODUCE
                        || ua.m_b.getType() == UnitAction.TYPE_HARVEST
                        || ua.m_b.getType() == UnitAction.TYPE_RETURN) {
                    toRemove.add(ua);
                    if (DEBUG >= 1) {
                        System.out.println("removed");
                    }
                }
            }
            for (Pair<Unit, UnitAction> ua : toRemove) {
                //rgs.removeUnit(ua.m_a);
                paTactics.getActions().remove(ua);
            }

            PlayerAction paFull = new PlayerAction();
            //add extra actions
            List<Unit> skip = new ArrayList<Unit>();
            for (Pair<Unit, UnitAction> ua : paTactics.getActions()) {
                // check to see if the action is legal!
                PhysicalGameState pgs = _gs.getPhysicalGameState();
                ResourceUsage r = ua.m_b.resourceUsage(ua.m_a, pgs);
                boolean targetOccupied = false;
                for (int position : r.getPositionsUsed()) {
                    int y = position / pgs.getWidth();
                    int x = position % pgs.getWidth();
                    if (pgs.getTerrain(x, y) != PhysicalGameState.TERRAIN_NONE
                            || pgs.getUnitAt(x, y) != null) {
                        targetOccupied = true;
                        break;
                    }
                }
                if (!targetOccupied && r.consistentWith(paStrategy.getResourceUsage(), _gs)) {
                    paFull.addUnitAction(ua.m_a, ua.m_b);
                    paFull.getResourceUsage().merge(r);
                    if (DEBUG >= 1) {
                        System.out.println("Frame: " + _gs.getTime() + ", extra action: " + ua);
                    }
                    skip.add(ua.m_a);
                } else {
                    if (DEBUG >= 1) {
                        System.out.println("inconsistent");
                    }
                }
            }

            //add script actions
            for (Pair<Unit, UnitAction> ua : paStrategy.getActions()) {
                boolean found = false;
                for (Unit u : skip) {
                    if (u.getID() == ua.m_a.getID()) {
                        found = true;
                        break;
                    }
                }
                if (found) {//skip units that were assigned by the extra AI
                    if (DEBUG >= 1) {
                        System.out.println("skipping");
                    }
                    continue;
                }
                paFull.addUnitAction(ua.m_a, ua.m_b);
                paFull.getResourceUsage().merge(ua.m_b.resourceUsage(ua.m_a, _gs.getPhysicalGameState()));
            }
            return paFull;
        }
    }

}
