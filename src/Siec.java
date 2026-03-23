
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Siec {
	Warstwa [] warstwy;
	int liczba_warstw;
	
	public Siec(){
		warstwy=null;
		this.liczba_warstw=0;
	}
	public Siec(int liczba_wejsc,int liczba_warstw,int [] lnww){
		this.liczba_warstw=liczba_warstw;
		warstwy=new Warstwa[liczba_warstw];
		for(int i=0;i<liczba_warstw;i++)
			warstwy[i]=new Warstwa((i==0)?liczba_wejsc:lnww[i-1],lnww[i]);
	}
	double [] oblicz_wyjscie(double [] wejscia){
		double [] wyjscie=null;
		for(int i=0;i<liczba_warstw;i++)
			wejscia = wyjscie = warstwy[i].oblicz_wyjscie(wejscia);
		return wyjscie;
	}

	double trainSample(double[] inputs,double[] expected,double learningRate){
		double[] output=oblicz_wyjscie(inputs);
		if(output==null || expected==null || output.length!=expected.length)
			throw new IllegalArgumentException("Output and expected vectors must have the same length.");

		int outputLayerIndex=liczba_warstw-1;
		Warstwa outputLayer=warstwy[outputLayerIndex];

		for(int i=0;i<outputLayer.liczba_neuronow;i++) {
			Neuron neuron=outputLayer.neurony[i];
			double out=neuron.lastOutput;
			neuron.deltaError=(expected[i]-out)*out*(1.0-out);
		}

		for(int layerIndex=outputLayerIndex-1;layerIndex>=0;layerIndex--) {
			Warstwa currentLayer=warstwy[layerIndex];
			Warstwa nextLayer=warstwy[layerIndex+1];
			for(int neuronIndex=0;neuronIndex<currentLayer.liczba_neuronow;neuronIndex++) {
				double weightedDeltaSum=0.0;
				for(int nextNeuronIndex=0;nextNeuronIndex<nextLayer.liczba_neuronow;nextNeuronIndex++) {
					Neuron nextNeuron=nextLayer.neurony[nextNeuronIndex];
					weightedDeltaSum+=nextNeuron.deltaError*nextNeuron.wagi[neuronIndex+1];
				}
				Neuron neuron=currentLayer.neurony[neuronIndex];
				double out=neuron.lastOutput;
				neuron.deltaError=out*(1.0-out)*weightedDeltaSum;
			}
		}

		for(int layerIndex=0;layerIndex<liczba_warstw;layerIndex++) {
			Warstwa layer=warstwy[layerIndex];
			double[] layerInputs=layer.lastInputs;
			for(int neuronIndex=0;neuronIndex<layer.liczba_neuronow;neuronIndex++) {
				Neuron neuron=layer.neurony[neuronIndex];
				neuron.wagi[0]+=learningRate*neuron.deltaError;
				for(int inputIndex=1;inputIndex<=neuron.liczba_wejsc;inputIndex++)
					neuron.wagi[inputIndex]+=learningRate*neuron.deltaError*layerInputs[inputIndex-1];
			}
		}

		double mse=0.0;
		for(int i=0;i<output.length;i++) {
			double error=expected[i]-output[i];
			mse+=error*error;
		}
		return mse/output.length;
	}

	double trainEpoch(double[][] data,double[][] expected,double learningRate){
		if(data==null || expected==null || data.length!=expected.length)
			throw new IllegalArgumentException("Data and expected arrays must have the same length.");
		if(data.length==0)
			return 0.0;

		List<Integer> indexes=new ArrayList<>();
		for(int i=0;i<data.length;i++)
			indexes.add(i);
		Collections.shuffle(indexes);

		double totalMse=0.0;
		for(int i=0;i<indexes.size();i++) {
			int sampleIndex=indexes.get(i);
			totalMse+=trainSample(data[sampleIndex],expected[sampleIndex],learningRate);
		}
		return totalMse/data.length;
	}
}
