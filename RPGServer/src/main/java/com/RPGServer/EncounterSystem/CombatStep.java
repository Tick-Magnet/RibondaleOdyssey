package com.RPGServer.EncounterSystem;

import com.RPGServer.Dice;
import com.RPGServer.PlayerCharacter;
import com.RPGServer.UserAccount;
import com.RPGServer.UserAccountRepository;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

public class CombatStep extends EncounterStep
{
	private int enemyIndex;
	private int nextStepIndex;
	private boolean stepWon;
	
	public CombatStep(int enemyIndex, int nextStepIndex)
	{
		this.enemyIndex = enemyIndex;
		this.nextStepIndex = nextStepIndex;
		stepWon = false;
	}	
	
	@Override
	public void endStep(int selectedChoice)
	{
		//Grant reward
		if(rewards[0] != null && stepWon)
		{
			parentEncounter.encounterRewards.add(rewards[0]);
		}
	}
	
	@Override
	public StepUpdate postStepUpdate(StepUpdate update)
	{
		Encounter.EncounterEntity enemy = parentEncounter.entityArray[enemyIndex];
		PlayerCharacter player = parentEncounter.playerAccount.playerCharacter;
		StepUpdate output;
		
		CombatStepUpdate combatUpdate = new CombatStepUpdate();
		combatUpdate.choices = new String[]{"attack", "leave"};
		//Process step update
		switch(update.selectedChoice)
		{
			//Player attacks
			case 0:
				//Figure damage using players character
				enemy.applyDamage(player.figureDamage());
			break;
			//Leave encounter
			case 1:
				//End step, signal end encounter
				endStep(0);
				
			break;
			
			default:
			break;
		}
		
		//Process entity's attack
		if(enemy.health > 0)
		{
			player.applyDamage(enemy.figureDamage());
		}
		//Figure damage from entities stats
		combatUpdate.enemyHealth = enemy.health;
		combatUpdate.playerHealth = player.getHealth();

		//If player health is zero, end encounter
		if(player.getHealth() <= 0)
		{
			parentEncounter.endEncounter();
		}
		//If enemy health is zero, end step
		//Return next initial step
		if(enemy.health <= 0)
		{
			endStep(0);
			parentEncounter.currentStep = parentEncounter.encounterSteps[nextStepIndex];
			output = parentEncounter.currentStep.getInitialStepUpdate();
		}
		else
		{
			output = combatUpdate;
		}
		return output;
	}


	
	@Override
	public StepUpdate getInitialStepUpdate()
	{
		CombatStepUpdate output = new CombatStepUpdate();
		output.playerHealth = 0;
		output.enemyHealth = parentEncounter.entityArray[enemyIndex].health;
		output.stepType = 1;
		return output;
	}
	
	public class CombatStepUpdate extends StepUpdate
	{
		int playerHealth;
		int enemyHealth;
		@Override
		public Map<String, Object> toMap()
		{
			Map<String, Object> output = super.toMap();
			//Add each relevant field to map
			output.put("playerHealth", playerHealth);
			output.put("enemyHealth", enemyHealth);
			
			return output;
		}
	}
}
