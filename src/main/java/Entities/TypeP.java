package Entities;

public enum TypeP {
    MOTO(1),VOITURE(2),CAMION(3);
    private int value;
    TypeP(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
    public TypeP typefromvalue(int value) {
        switch (value) {
            case 1:
                return MOTO;
            case 2:
                return VOITURE;
            case 3:
                return CAMION;
            default:
                return null;
        }
    }

    @Override
    public String toString() {
        String ch ="";
        switch (value) {
            case 1:
                ch = "MOTO";
                break;
            case 2:
                ch = "VOITURE";
                break;
            case 3:
                ch = "CAMION";
                break;
            default:
                break;

        }
        return ch;
    }
}
