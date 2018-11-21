package com.cit.rules;

import com.cit.transfer.ValidationRulesResult;
import com.deliveredtechnologies.rulebook.lang.RuleBuilder;
import com.deliveredtechnologies.rulebook.model.rulechain.cor.CoRRuleBook;

public class EventValidationRuleBook extends CoRRuleBook<ValidationRulesResult> {

    public static final String POSSIBLE_TIME_DISTANCE_EVENT = "Possible time-distance event";
    public static final String IMPOSSIBLE_TIME_DISTANCE_EVENT = "Impossible time-distance event";


    @Override
    public void defineRules() {


        //  add a rule which evaluates if the current event and the last event were at the same panel location
        addRule(RuleBuilder.create().withFactType(EventValidationBean.class).withResultType(ValidationRulesResult.class)
                .when(facts ->
                        facts.getOne().isThePanelsTheSameforCurrentAndPreviousEvents()
                )
                .using("isThePanelsTheSameforCurrentAndPreviousEvents()")
                .then((facts, result) -> result.setValue( ValidationRulesResult.builder().reason(POSSIBLE_TIME_DISTANCE_EVENT).validEvent(true).build() ))
                .stop()
                .build());


        //  add a rule which evaluates if its possible to walk between events
        addRule(RuleBuilder.create().withFactType(EventValidationBean.class).withResultType(ValidationRulesResult.class)
                .when(facts ->
                        facts.getOne().isItPossibleToWalkBetweenCurrentAndPreviousEvent()
                )
                .using("isItPossibleToWalkBetweenCurrentAndPreviousEvent()")
                .then((facts, result) -> result.setValue( ValidationRulesResult.builder().reason(POSSIBLE_TIME_DISTANCE_EVENT).validEvent(true).build() ))
                .stop()
                .build());

        //  evaluate only if preceding rules fails
        //  add a rule which evaluates if not possible to drive between events
        addRule(RuleBuilder.create().withFactType(EventValidationBean.class).withResultType(ValidationRulesResult.class)
                .when(facts ->
                        facts.getOne().isItPossibleToDriveBetweenCurrentAndPreviousEvent()
                )
                .using(".isItPossibleToDriveBetweenCurrentAndPreviousEvent()")
                .then((facts, result) -> result.setValue( ValidationRulesResult.builder().reason(POSSIBLE_TIME_DISTANCE_EVENT).validEvent(true).build() ))
                .stop()
                .build());

        //  evaluate only if preceding rules fails
        //  add a rule which evaluates if its not possible to fly + drive between events
        addRule(RuleBuilder.create().withFactType(EventValidationBean.class).withResultType(ValidationRulesResult.class)
                .when(facts ->
                        facts.getOne().isItPossibleToFlyAndDriveBetweenCurrentAndPreviousEvent()
                )
                .using(".isItPossibleToFlyAndDriveBetweenCurrentAndPreviousEvent()")
                .then((facts, result) -> result.setValue( ValidationRulesResult.builder().reason(POSSIBLE_TIME_DISTANCE_EVENT).validEvent(true).build() ))
                .stop()
                .build());

        //  evaluate only if preceding rules fails
        //  add this rule so that if all other rules fail it must be an immpossible event
        addRule(RuleBuilder.create().withFactType(EventValidationBean.class).withResultType(ValidationRulesResult.class)
                .using("add this rule so that if all other rules fail it must be an immpossible event")
                .then((facts, result) -> result.setValue( ValidationRulesResult.builder().reason(IMPOSSIBLE_TIME_DISTANCE_EVENT).validEvent(false).build() ))
                .stop()
                .build());



/*

From
	CA, USA
To
	London, UK
Route
	LHW-BOS
	Flights
		norweigian 4:20 pm - 6:50 pm
		delta  9:20 am  - 12:19 pm
		virgin 4:35 PM - 7:15 pm
		BA  7:10pm - 9:50 PM
	Earliest departure 9:20 am
	Latest	departure 7:pm
	Duration 7 hr 50m



 */


    }

}
