# ComplexDataObject

A library that models real-world objects in Java, referred to as ComplexDataObjects. 
Other features: IO and preprocessing of ComplexDataObjects.

Requires Java 21 or later.

## Usage

Maven coordinates:

```xml
<dependency>
  <groupId>com.github.tknudsen</groupId>
  <artifactId>complex-data-object</artifactId>
  <version>0.2.14</version>
</dependency>
```

Current snapshot:

```xml
<dependency>
  <groupId>com.github.tknudsen</groupId>
  <artifactId>complex-data-object</artifactId>
  <version>0.3.0-SNAPSHOT</version>
</dependency>
```

## Core Data Structures

- **`KeyValueObject`** -- base string-keyed attribute store; the foundation everything else builds on.
- **`ComplexDataObject`** -- extends `KeyValueObject` to model an arbitrary real-world entity (name, description, change notifications).
- **`ComplexDataContainer`** -- a collection of `ComplexDataObject`s, keyed by a configurable primary-key attribute.
- **`DataSchema` / `DataSchemaEntry`** -- describes the attribute set of a dataset (name, type, default value), analogous to a table header.
- **Feature vectors** (`data.features`) -- `AbstractFeatureVector` and its specializations: `NumericalFeatureVector` (pure `double` vectors for ML/data-mining), `MixedDataFeatureVector` (mixed Double/String/Boolean features), `CategoricalFeature`.
- **`Ranking<T>`** -- a sorted collection optimized for fast insertion/removal anywhere in the order.
- **Distance matrices** (`data.distanceMatrix`) -- `IDistanceMatrix` and its implementations (`DistanceMatrix`, `DistanceMatrixBlockedParallel`, `PairwiseDistancesMatrix`) store and compute pairwise distances over a fixed element set, with parallel/blocked variants for large inputs.
- **Uncertainty** (`data.uncertainty`) -- `IUncertainty` -> `ValueUncertainty` -> `ValueUncertaintyRange` -> `ValueUncertaintyDistribution`, an increasingly detailed statistical characterization of how uncertain a value is (magnitude, then bounds, then full distribution stats).

## Data Analysis Interfaces

The library separates *what an operation does* (an interface) from *how it does it* (an implementation), so most analysis steps are pluggable:

- **`IDistanceMeasure<T>`** -- the base contract for any distance/similarity function; implementations exist for Boolean, Double, String, and feature-vector types (Euclidean, Manhattan, Chebyshev, Levenshtein, weighted variants, ...).
- **`IComplexDataObjectProcessor` / `INumericalFeatureVectorProcessor` / `IMixedDataFeatureVectorProcessor`** -- in-place processing/transformation steps (normalization, outlier treatment, feature selection, attribute conversion) for each of the three core data shapes.
- **`IDescriptor<I,O>`** -- transforms real-world objects (e.g. `ComplexDataObject`) into feature space.
- **`IDimensionalityReduction<X,Y>`** -- reduces feature vectors to a lower-dimensional mapping.
- **`AttributeScoringFunction<T>`** -- maps a single attribute to a normalized score, handling parsing, normalization, and missing values.
- **`IDataMiningWorkflow<O,F,FV>`** -- ties the above together into a pipeline: pre-processors condition raw objects, a descriptor extracts feature vectors, feature processors refine them, and a distance measure enables comparison of the result.
- **`IObjectParser<T>`** -- typed value parsing (`DoubleParser`, `DateParser`, `BooleanParser`, ...) used throughout CSV/schema/attribute-type detection.
