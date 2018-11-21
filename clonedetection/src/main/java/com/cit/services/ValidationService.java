package com.cit.services;


import com.cit.model.Event;
import com.cit.rules.EventValidationBean;
import com.cit.transfer.ValidationRulesResult;
import com.cit.transfer.ValidationServiceRestResponseDTO;
import com.deliveredtechnologies.rulebook.FactMap;
import com.deliveredtechnologies.rulebook.NameValueReferableMap;
import com.deliveredtechnologies.rulebook.Result;
import com.deliveredtechnologies.rulebook.model.RuleBook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ValidationService implements IValidationService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private RuleBook eventRuleBook;
    private IDistanceService distanceService;


    @Autowired
    public ValidationService(RuleBook eventRuleBook, IDistanceService distanceService) {
        this.eventRuleBook=eventRuleBook;
        this.distanceService=distanceService;
    }



    @Override
    public ValidationServiceRestResponseDTO performEventValidation(Event current, Event previous) {


        // default to possible result
        ValidationRulesResult validationRulesResult = ValidationRulesResult.builder()
                .reason("Possible time-distance event.")
                .validEvent(true)
                .build();

        if (log.isDebugEnabled()) {
            log.debug("PerformEventValidation(CurrentEvent={} , PreviousEvent={})", current, previous );
        }

        if (previous != null) {

            NameValueReferableMap facts = new FactMap();
            facts.setValue("eventValidation", new EventValidationBean(current,previous,distanceService));

            eventRuleBook.run(facts);

            Optional<Result> optionalResult = eventRuleBook.getResult();
            if (optionalResult.isPresent()) {

                ValidationRulesResult result = (ValidationRulesResult) optionalResult.get().getValue();
                validationRulesResult.setReason(result.getReason());
                validationRulesResult.setValidEvent(result.isValidEvent());
            }

        } else {  // must be a valid Event
            if (log.isDebugEnabled()) {
                log.debug("Can not find previous event for cardId={} , Event={}" ,current.getCardId(), current );
            }
        }

        //
        // Build the response DTO
        //
        return ValidationServiceRestResponseDTO.builder()
                .currentEvent(current)
                .previousEvent(previous)
                .reason(validationRulesResult.getReason())
                .validEvent(validationRulesResult.isValidEvent())
                .build();

    }
}
