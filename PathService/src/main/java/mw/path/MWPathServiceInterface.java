package mw.path;

public interface MWPathServiceInterface {
    public String[] calculatePath(String startID, String endID) throws MWNoPathException;
}