import sys
from org.apache.pig.scripting import Pig

load = Pig.compileFromFile(sys.argv[1])
iteration = Pig.compileFromFile('iteration.pig')
store = Pig.compileFromFile('store.pig')
 
print '*** Loading input ***' 
load_stats = load.bind({'EDGES_OUT': 'edges0.tmp'}).runSingle()
if not load_stats.isSuccessful():
    raise 'Load failed'

i = 1
stable_inerations = 0
edges_in = 'edges' + str(i - 1) + '.tmp'
edges_out = ''

while True:
    print "*** Iteration " + str(i) + " ***"
    edges_out = 'edges' + str(i) + '.tmp'
    iteration_bound = iteration.bind({'EDGES_IN': edges_in, 'EDGES_OUT': edges_out, 
        'CONVERGENCE_OUT': 'convergence.tmp'})
    iteration_stats = iteration_bound.runSingle()
    if not iteration_stats.isSuccessful():
        raise 'Iteration failed'
    conv_result = iteration_stats.result('convergence').iterator().next()
    max_iter = int(str(conv_result.get(0)))
    conv_iter = int(str(conv_result.get(1)))
    change_count = int(str(conv_result.get(2)))
    Pig.fs('rm -r ' + 'convergence.tmp')
    Pig.fs('rm -r ' + edges_in)
    edges_in = edges_out
    print "Decision change count: " + str(change_count)
    if change_count == 0:
        stable_iterations += 1
    else:
        stable_iterations = 0
    print "Stable iterations: " + str(stable_iterations)
    print "Convergence iterations: " + str(conv_iter)
    print "Max iterations: " + str(max_iter)
    if stable_iterations >= conv_iter:
        print "Stopping due to convergence"
        break
    if i >= max_iter:
        print "Stopping due to max iterations reached"
        break
    i += 1

print '*** Writing result ***'
store_stats = store.bind({'EDGES_IN': edges_in}).runSingle();
if not store_stats.isSuccessful():
    raise 'Store failed'
Pig.fs('rm -r ' + edges_in)
