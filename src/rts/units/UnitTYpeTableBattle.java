/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rts.units;

/**
 *
 * @author rubens
 */
public class UnitTYpeTableBattle extends UnitTypeTable {

    @Override
    public void setUnitTypeTable(int version, int crs) {
        moveConflictResolutionStrategy = crs;

        if (version == EMPTY_TYPE_TABLE) {
            return;
        }

        // Create the unit types:
        // RESOURCE:
        UnitType resource = new UnitType();
        resource.name = "Resource";
        resource.isResource = true;
        resource.isStockpile = false;
        resource.canHarvest = false;
        resource.canMove = false;
        resource.canAttack = false;
        resource.sightRadius = 0;
        addUnitType(resource);

        // BASE:
        UnitType base = new UnitType();
        base.name = "Base";
        base.cost = 10;
        base.hp = 10;
        switch (version) {
            case VERSION_ORIGINAL:
                base.produceTime = 250;
                break;
            case VERSION_ORIGINAL_FINETUNED:
                base.produceTime = 200;
                break;
        }
        base.isResource = false;
        base.isStockpile = true;
        base.canHarvest = false;
        base.canMove = false;
        base.canAttack = false;
        base.sightRadius = 5;
        addUnitType(base);

        // BARRACKS: 
        UnitType barracks = new UnitType();
        barracks.name = "Barracks";
        barracks.cost = 5;
        barracks.hp = 4;
        switch (version) {
            case VERSION_ORIGINAL:
                barracks.produceTime = 200;
                break;
            case VERSION_ORIGINAL_FINETUNED:
            case VERSION_NON_DETERMINISTIC:
                barracks.produceTime = 100;
                break;
        }
        barracks.isResource = false;
        barracks.isStockpile = false;
        barracks.canHarvest = false;
        barracks.canMove = false;
        barracks.canAttack = false;
        barracks.sightRadius = 3;
        addUnitType(barracks);

        // WORKER: 
        UnitType worker = new UnitType();
        worker.name = "Worker";
        worker.cost = 1;
        worker.hp = 1;
        switch (version) {
            case VERSION_ORIGINAL:
            case VERSION_ORIGINAL_FINETUNED:
                worker.minDamage = worker.maxDamage = 1;
                break;
            case VERSION_NON_DETERMINISTIC:
                worker.minDamage = 0;
                worker.maxDamage = 2;
                break;
        }
        worker.attackRange = 1;
        worker.produceTime = 50;
        worker.moveTime = 10;
        worker.attackTime = 5;
        worker.harvestTime = 20;
        worker.returnTime = 10;
        worker.isResource = false;
        worker.isStockpile = false;
        worker.canHarvest = true;
        worker.canMove = true;
        worker.canAttack = true;
        worker.sightRadius = 3;
        addUnitType(worker);

        // LIGHT: 
        UnitType light = new UnitType();
        light.name = "Light";
        light.cost = 2;
        light.hp = 8;
        switch (version) {
            case VERSION_ORIGINAL:
            case VERSION_ORIGINAL_FINETUNED:
                light.minDamage = light.maxDamage = 2;
                break;
            case VERSION_NON_DETERMINISTIC:
                light.minDamage = 1;
                light.maxDamage = 3;
                break;
        }
        light.attackRange = 1;
        light.produceTime = 80;
        light.moveTime = 8;
        light.attackTime = 5;
        light.isResource = false;
        light.isStockpile = false;
        light.canHarvest = false;
        light.canMove = true;
        light.canAttack = true;
        light.sightRadius = 2;
        addUnitType(light);

        // HEAVY: 
        UnitType heavy = new UnitType();
        heavy.name = "Heavy";
        switch (version) {
            case VERSION_ORIGINAL:
            case VERSION_ORIGINAL_FINETUNED:
                heavy.minDamage = heavy.maxDamage = 4;
                break;
            case VERSION_NON_DETERMINISTIC:
                heavy.minDamage = 0;
                heavy.maxDamage = 6;
                break;
        }
        heavy.attackRange = 1;
        heavy.produceTime = 120;
        switch (version) {
            case VERSION_ORIGINAL:
                heavy.moveTime = 12;
                heavy.hp = 8;
                heavy.cost = 2;
                break;
            case VERSION_ORIGINAL_FINETUNED:
            case VERSION_NON_DETERMINISTIC:
                heavy.moveTime = 10;
                heavy.hp = 16;
                heavy.cost = 3;
                break;
        }
        heavy.attackTime = 5;
        heavy.isResource = false;
        heavy.isStockpile = false;
        heavy.canHarvest = false;
        heavy.canMove = true;
        heavy.canAttack = true;
        heavy.sightRadius = 2;
        addUnitType(heavy);

        // RANGED: 
        UnitType ranged = new UnitType();
        ranged.name = "Ranged";
        ranged.cost = 2;
        ranged.hp = 2;
        switch (version) {
            case VERSION_ORIGINAL:
            case VERSION_ORIGINAL_FINETUNED:
                ranged.minDamage = ranged.maxDamage = 1;
                break;
            case VERSION_NON_DETERMINISTIC:
                ranged.minDamage = 1;
                ranged.maxDamage = 2;
                break;
        }
        ranged.attackRange = 3;
        ranged.produceTime = 100;
        ranged.moveTime = 10;
        ranged.attackTime = 5;
        ranged.isResource = false;
        ranged.isStockpile = false;
        ranged.canHarvest = false;
        ranged.canMove = true;
        ranged.canAttack = true;
        ranged.sightRadius = 3;
        addUnitType(ranged);

        base.produces(worker);
        barracks.produces(light);
        barracks.produces(heavy);
        barracks.produces(ranged);
        worker.produces(base);
        worker.produces(barracks);
    }

}
