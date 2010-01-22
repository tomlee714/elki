package experimentalcode.lisa;


import de.lmu.ifi.dbs.elki.result.outlier.ProbabilisticOutlierScore;

import java.util.HashMap;

import de.lmu.ifi.dbs.elki.result.AnnotationFromHashMap;
import de.lmu.ifi.dbs.elki.result.AnnotationResult;
import de.lmu.ifi.dbs.elki.result.OrderingFromHashMap;
import de.lmu.ifi.dbs.elki.result.OrderingResult;
import de.lmu.ifi.dbs.elki.result.outlier.OutlierResult;
import de.lmu.ifi.dbs.elki.result.outlier.OutlierScoreMeta;
import java.util.List;

import de.lmu.ifi.dbs.elki.algorithm.DistanceBasedAlgorithm;
import de.lmu.ifi.dbs.elki.data.DatabaseObject;
import de.lmu.ifi.dbs.elki.database.AssociationID;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.distance.Distance;
import de.lmu.ifi.dbs.elki.result.MultiResult;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.OptionID;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.ParameterException;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.PatternParameter;
/**
 * Simple distanced based outlier detection algorithm. User has to specify two parameters 
 * An object is flagged as an outlier if at least a fraction p of all data objects has a distance above d from c
 * <p>Reference: 
 *  E.M. Knorr, R.
 * T. Ng: Algorithms for Mining Distance-Based Outliers in Large Datasets, In:
 * Procs Int. Conf. on Very Large Databases (VLDB'98), New York, USA, 1998.
 * 
 * This paper presents several Distance Based Outlier Detection algorithms.
 * Implemented here is a simple index based algorithm as presented in section
 * 3.1.
 * 
 * @author Lisa Reichert
 * 
 * @param <O> the type of DatabaseObjects handled by this Algorithm
 * @param <D> the type of Distance used by this Algorithm
 */
public abstract class AbstractDBOutlierDetection<O extends DatabaseObject, D extends Distance<D>> extends DistanceBasedAlgorithm<O, D, MultiResult> {

  /**
   * Association ID for DBOD.
   */
  public static final AssociationID<Double> DBOD_SCORE = AssociationID.getOrCreateAssociationID("dbod.score", Double.class);

  /**
   * OptionID for {@link #D_PARAM}
   */
  public static final OptionID D_ID = OptionID.getOrCreateOptionID("dbod.d", "size of the D-neighborhood");

  /**
   * Parameter to specify the size of the D-neighborhood,
   * 
   * <p>
   * Key: {@code -dbod.d}
   * </p>
   */
  private final PatternParameter D_PARAM = new PatternParameter(D_ID);

  /**
   * Holds the value of {@link #D_PARAM}.
   */
  private String d;

 
  /**
   * Provides the result of the algorithm.
   */
  MultiResult result;

  /**
   * Constructor, adding options to option handler.
   */
  public AbstractDBOutlierDetection() {
    super();
    // neighborhood size
    addOption(D_PARAM);
  }

  /**
   * Calls the super method and sets additionally the values of the parameter
   * {@link #D_PARAM}, {@link #P_PARAM}
   */
  @Override
  public List<String> setParameters(List<String> args) throws ParameterException {
    List<String> remainingParameters = super.setParameters(args);
    // neighborhood size
    d = D_PARAM.getValue();
    return remainingParameters;
  }

  /**
   * Runs the algorithm in the timed evaluation part.
   * 
   */
  @Override
  protected MultiResult runInTime(Database<O> database) throws IllegalStateException {
    getDistanceFunction().setDatabase(database, isVerbose(), isTime());
    
    if(this.isVerbose()) {
      this.verbose("computing outlier flag");
    }
    
    HashMap<Integer, Double> dbodscore = new HashMap<Integer, Double>();
    dbodscore = computeOutlierScores(database, d);

    
    // Build result representation.
    AnnotationResult<Double> scoreResult = new AnnotationFromHashMap<Double>(DBOD_SCORE, dbodscore);
    OrderingResult orderingResult = new OrderingFromHashMap<Double>(dbodscore, true);
    OutlierScoreMeta scoreMeta = new ProbabilisticOutlierScore();
    this.result = new OutlierResult(scoreMeta, scoreResult, orderingResult);
 
    return result;

  }
  /**
   * computes an outlier score for each object of the database.
   */
  protected abstract HashMap<Integer, Double> computeOutlierScores(Database<O> database, String d);


@Override
  public MultiResult getResult() {
    return result;
  }
}
