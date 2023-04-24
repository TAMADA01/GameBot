package org.GameBot.GameBot.Bot;

public enum StateBot {
    None{
        @Override
        public StateBot nextState(){
            return this;
        }
    },
    WriteDiscription{
        @Override
        public StateBot nextState(){
            return WriteReward;
        }
    },
    WriteReward{
        @Override
        public StateBot nextState(){
            return this;
        }
    },
    FinishMission{
        @Override
        public StateBot nextState(){
            return this;
        }
    },
    RewardUser{
        @Override
        public StateBot nextState(){
            return this;
        }
    };

    public abstract StateBot nextState();
}
