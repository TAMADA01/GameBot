package org.GameBot.GameBot.Bot;

public enum StateBot {
    None{
        @Override
        public StateBot nextState(){
            return CreatePet;
        }
    },
    CreatePet{
        @Override
        public StateBot nextState(){
            return this;
        }
    };

    public abstract StateBot nextState();
}
